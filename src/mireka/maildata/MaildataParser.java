package mireka.maildata;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import static mireka.maildata.MaildataParser.TokenKind.*;

/**
 * MailDataParser is a top level parser for mail data, it separates the heading
 * section and the body. It separates and unfolds the header fields but it does
 * not parse them.
 * 
 * TODO: include syntax rules here
 */
public class MaildataParser {

    private Token currentToken;
    private StringBuilder spelling = new StringBuilder(4096);
    private Scanner scanner;

    public MaildataParser(InputStream in) throws IOException {
        if (in == null)
            throw new NullPointerException();
        
        scanner = new Scanner(in);
    }

    public Result parse() throws IOException {
        try {
            currentToken = scanner.scan();
            return parseMailData();
        } catch (ParseException e) {
            // even malformed mail should be parsed without exception 
            throw new RuntimeException("Unexpted exception", e);
        }
    }

    private Result parseMailData() throws ParseException, IOException {
        Result result = new Result();

        result.headerSection = parseHeaderSection();
        switch (currentToken.kind) {
        case CRLF:
            result.bodyPosition = currentToken.position
                    + currentToken.spelling.length();
            result.separator = currentToken.spelling;
            break;
        case EOF:
            // nothing to do
            break;
        default:
            throw currentToken
                    .unexpectedHereSyntaxException("The CRLF separator between the heading section and the body");
        }
        return result;
    }

    private HeaderSection parseHeaderSection() throws ParseException,
            IOException {
        HeaderSection result = new HeaderSection();

        //TODO: The very first heading field must be accepted even if it is 
        // malformed and does not start with UTEXT.
        while (currentToken.kind == UTEXT) {
            HeaderFieldText field = parseFoldedHeaderField();
            result.addExtracted(field);
        }

        return result;
    }

    private HeaderFieldText parseFoldedHeaderField() throws IOException,
            ParseException {
        spelling.setLength(0);
        StringBuilder unfolded = new StringBuilder(1024);

        String line = parseFoldedHeaderFieldFirstLine();
        unfolded.append(line);

        while (currentToken.kind == WS) {
            line = parseFoldedHeaderFieldAdditionalLine();
            unfolded.append(line);
        }

        unfolded.append("\r\n");
        HeaderFieldText result = new HeaderFieldText();
        result.originalSpelling = spelling.toString();
        result.unfoldedSpelling = unfolded.toString();
        return result;

    }

    private String parseFoldedHeaderFieldFirstLine() throws IOException,
            ParseException {
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

    private String parseFoldedHeaderFieldAdditionalLine() throws IOException,
            ParseException {
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

    private void acceptIt() throws IOException {
        spelling.append(currentToken.spelling);
        currentToken = scanner.scan();
    }

    private void accept(TokenKind requiredKind) throws ParseException,
            IOException {
        if (currentToken.kind == requiredKind)
            acceptIt();
        else
            throw currentToken.syntaxException(requiredKind);
    }

    /**
     * 
     */
    private class Scanner {
        private InputStream in;
        private int currentChar;
        private long position;
        private StringBuilder currentSpelling = new StringBuilder();

        public Scanner(InputStream in) throws IOException {
            this.in = in;
            currentChar = in.read();
        }

        public Token scan() throws IOException {
            currentSpelling.setLength(0);
            Token token = new Token();
            token.position = position;

            token.kind = scanToken();
            token.spelling = currentSpelling.toString();
            return token;
        }

        private TokenKind scanToken() throws IOException {
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

        private void takeIt() throws IOException {
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

        public ParseException unexpectedHereSyntaxException(String where) {
            return new ParseException("Syntax error. Unexpected token: '"
                    + toString() + "', at the position: '" + where
                    + "' at character position " + position + ".",
                    (int) position);
        }
    }
    
    public static class Result {
        /**
         * It may be an empty string, although that is semantically invalid.
         */
        HeaderSection headerSection;
        /**
         * Null, if no separator presents, which also means that there is no body.
         */
        String separator;
        /**
         * -1 means that there is no body, this happens when no separator is found.
         * It may point to EOF, if the body is empty.
         */
        long bodyPosition = -1;
    }
}
