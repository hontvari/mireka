package mireka.maildata.parser;

import static mireka.maildata.parser.MaildataParser.TokenKind.*;

import java.text.ParseException;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mireka.maildata.HeaderField;
import mireka.maildata.HeaderFieldText;
import mireka.maildata.ast.Fields;
import mireka.maildata.ast.MessageNode;
import mireka.maildata.field.ContentType;
import mireka.maildata.field.MimeVersion;
import mireka.maildata.io.MaildataInputStream;
import mireka.maildata.io.MaildataReadException;
import mireka.maildata.io.Subsource;

/**
 * MaildataParser is a top level parser for mail data, it separates the heading section and the
 * body. It separates and unfolds the header fields but it does not parse them.
 * 
 * TODO: include syntax rules here
 */
public class MaildataParser implements AutoCloseable {
    private final Logger logger = LoggerFactory.getLogger(MaildataParser.class);
    private final MaildataInputStream in;
    private final Subsource source;

    private Token currentToken;
    private StringBuilder spelling = new StringBuilder(4096);
    private Scanner scanner;

    public MaildataParser(MaildataInputStream in)
            throws MaildataReadException {
        Objects.requireNonNull(in);
        this.in = in;
        this.source = in.source;
        scanner = new Scanner(in);
    }

    /**
     * Parses the maildata, it reads the heading section and the separator if exists, and it
     * calculates the range of the body but does not read its bytes.
     */
    public MessageNode parse() throws MaildataReadException {
        try {
            MessageNode r = new MessageNode();
            currentToken = scanner.scan();
            r.fields = parseFields();
            r.hasSeparator = currentToken.kind == CRLF;
            r.fieldsAndSeparatorSource = source.left(in.position);
            if (r.hasSeparator)
                r.bodySource = source.subSkipping(in.position);
            r.source = source;
            return r;
        } catch (ParseException e) {
            // even malformed mail should be parsed without exception
            throw new RuntimeException("Unexpted exception", e);
        }
    }

    private Fields parseFields() throws ParseException {
        Fields r = new Fields();

        // TODO: The very first heading field must be accepted even if it is
        // malformed and does not start with UTEXT.
        while (currentToken.kind == UTEXT) {
            HeaderFieldText fieldText = parseFoldedHeaderField();
            r.texts.add(fieldText);
            storeParsedFieldValue(r, fieldText);
        }

        return r;
    }

    private HeaderFieldText parseFoldedHeaderField() throws ParseException {
        spelling.setLength(0);
        StringBuilder unfolded = new StringBuilder(1024);

        String line = parseFoldedHeaderFieldFirstLine();
        unfolded.append(line);

        while (currentToken.kind == WS) {
            line = parseFoldedHeaderFieldAdditionalLine();
            unfolded.append(line);
        }

        HeaderFieldText result = new HeaderFieldText();
        result.originalSpelling = spelling.toString();
        result.unfoldedSpelling = unfolded.toString();
        return result;

    }

    private String parseFoldedHeaderFieldFirstLine() throws ParseException {
        StringBuilder result = new StringBuilder(80);

        result.append(currentToken.spelling);
        acceptIt();

        while (currentToken.kind == UTEXT || currentToken.kind == WS) {
            result.append(currentToken.spelling);
            acceptIt();
        }

        accept(CRLF);

        return result.toString();
    }

    private String parseFoldedHeaderFieldAdditionalLine() throws ParseException {
        StringBuilder result = new StringBuilder(80);

        result.append(currentToken.spelling);
        acceptIt();

        while (currentToken.kind == UTEXT || currentToken.kind == WS) {
            result.append(currentToken.spelling);
            acceptIt();
        }

        accept(CRLF);

        return result.toString();
    }

    private void storeParsedFieldValue(Fields fields, HeaderFieldText t) {
        HeaderField f;
        try {
            f = FieldParser.parse(t);
            switch (f.kind) {
            case MIME_VERSION:
                MimeVersion mimeVersion = (MimeVersion) f;
                fields.isMime = mimeVersion.major == 1 && mimeVersion.minor == 0;
                break;
            case CONTENT_TYPE:
                fields.mediaType = ((ContentType) f).mediaType;
                break;
            default:
                break;
            }
        } catch (ParseException e) {
            // if it has a syntax error than still continue with other fields, we should not drop a
            // message, at least not without a kind of 'strict' option.
            logger.warn("Header field syntax error", e);
        }
    }

    private void acceptIt() throws MaildataReadException {
        spelling.append(currentToken.spelling);
        currentToken = scanner.scan();
    }

    private void accept(TokenKind requiredKind) throws ParseException,
            MaildataReadException {
        if (currentToken.kind == requiredKind)
            acceptIt();
        else
            throw currentToken.syntaxException(requiredKind);
    }

    @Override
    public void close() {
        try {
            scanner.in.close();
        } catch (MaildataReadException e) {
            logger.error("closing of input stream failed", e);
        }
    }

    /**
     * 
     */
    private class Scanner {
        private MaildataInputStream in;
        private int currentChar;
        private long position = 0;
        private StringBuilder currentSpelling = new StringBuilder();

        public Scanner(MaildataInputStream in)
                throws MaildataReadException {
            this.in = in;
            currentChar = in.read();
        }

        public Token scan() throws MaildataReadException {
            currentSpelling.setLength(0);
            Token token = new Token();
            token.position = position;

            token.kind = scanToken();
            token.spelling = currentSpelling.toString();
            return token;
        }

        private TokenKind scanToken() {
            switch (currentChar) {
            case '\r':
                takeIt();
                if (currentChar == '\n') {
                    takeIt();
                    return CRLF;
                } else {
                    return UTEXT;
                }
            case -1:
                return EOF;
            case ' ':
            case '\t':
                takeIt();
                return WS;
            default:
                takeIt();
                return UTEXT;
            }
        }

        private void takeIt() {
            if (currentChar != -1)
                currentSpelling.append((char) currentChar);
            position++;
            currentChar = in.read();
        }

    }

    /**
     * It should be private, but then static import would not work.
     */
    enum TokenKind {
        CRLF, EOF,
        /**
         * Any character except WS and CRLF.
         */
        UTEXT,
        /**
         * Whitespace, either Tab or Space
         */
        WS
    };

    private class Token {
        public TokenKind kind;
        public long position;
        public String spelling;

        public ParseException syntaxException(TokenKind expected) {
            return new ParseException("Syntax error. Expected: "
                    + expected.toString() + ", received: " + toString()
                    + " at character position " + position + ".",
                    (int) position);
        }

        @SuppressWarnings("unused")
        public ParseException unexpectedHereSyntaxException(String where) {
            return new ParseException("Syntax error. Unexpected token: '"
                    + toString() + "', at the position: '" + where
                    + "' at character position " + position + ".",
                    (int) position);
        }
    }
}
