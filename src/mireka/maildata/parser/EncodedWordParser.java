package mireka.maildata.parser;

import static mireka.maildata.parser.EncodedWordParser.TokenKind.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

import javax.xml.bind.DatatypeConverter;

import mireka.util.CharsetUtil;

import org.subethamail.smtp.util.TextUtils;

/**
 * @see <a href="https://tools.ietf.org/html/rfc2047">RFC 2047 - MIME
 *      (Multipurpose Internet Mail Extensions) Part Three: Message Header
 *      Extensions for Non-ASCII Text</a>
 * @see <a href="https://tools.ietf.org/html/rfc2231">RFC 2231 - MIME Parameter
 *      Value and Encoded Word Extensions: Character Sets, Languages, and
 *      Continuations
 */
public class EncodedWordParser {
    private Token currentToken;
    private Scanner scanner;

    public String parse(String src) throws ParseException {
        scanner =
                new Scanner(new ByteArrayInputStream(
                        TextUtils.getAsciiBytes(src)));
        return parse();
    }

    /**
     * <pre>
     * encoded-word = "=?" charset "?" encoding "?" encoded-text "?="
     * charset = token
     * encoding = token
     * token = 1*<Any CHAR except SPACE, CTLs, and especials>
     * especials = "(" / ")" / "<" / ">" / "@" / "," / ";" / ":" / "
     *             <"> / "/" / "[" / "]" / "?" / "." / "="
     * encoded-text = 1*<Any printable ASCII character other than "?"
     *                   or SPACE>
     *                ; (but see "Use of encoded-words in message
     *                ; headers", section 5)
     * </pre>
     * 
     * Note: RFC 2231 makes possible to add a language tag after the charset,
     * using a '*' character as separator. The '*' has been character already
     * been permitted in the charset nonterminal, it was not a special
     * character. This would make parsing difficult. The charset and the
     * encoding would require different scanners. Instead of this, we can simply
     * truncate the charset content after the '*'.
     * 
     * <pre>
     * encoded-word := "=?" charset ["*" language] "?" encoding "?" encoded-text "?="
     * </pre>
     */
    private String parse() throws ParseException {
        currentToken = scanner.scan();
        accept(EQUAL);
        accept(QUESTION);
        String charset = currentToken.spelling;
        charset = trimLanguageFromCharset(charset);
        accept(ATOKEN);
        accept(QUESTION);
        String encoding = currentToken.spelling;
        accept(ATOKEN);
        acceptButDontScanNextToken(QUESTION);
        currentToken = scanner.scanEncodedText();
        String encodedText = currentToken.spelling;
        accept(ENCODED_TEXT);
        accept(QUESTION);
        accept(EQUAL);
        accept(EOF);

        switch (CharsetUtil.toAsciiLowerCase(encoding)) {
        case "b":
            return decodeBEncodedText(charset, encodedText);
        case "q":
            return decodeQEncodedText(charset, encodedText);
        default:
            return encodedText;
        }
    }

    private String trimLanguageFromCharset(String charset) {
        int iAsterisk = charset.indexOf('*');
        if (iAsterisk == -1)
            return charset;
        else
            return charset.substring(0, iAsterisk);
    }

    public static boolean isEncodedWord(String src) {
        return src.startsWith("=?") && src.endsWith("?=");
    }

    private void acceptIt() {
        currentToken = scanner.scan();
    }

    private void accept(TokenKind requiredKind) throws ParseException {
        if (currentToken.kind == requiredKind)
            acceptIt();
        else
            throw currentToken.syntaxException(requiredKind);
    }

    /**
     * Checks the current token, but does not scan the next token, this is
     * useful before switching to a different scanner.
     */
    private void acceptButDontScanNextToken(TokenKind requiredKind)
            throws ParseException {
        if (currentToken.kind != requiredKind)
            throw currentToken.syntaxException(requiredKind);
    }

    private String decodeQEncodedText(String charset, String encodedText)
            throws ParseException {
        try {
            byte[] bytes = new QEncodingParser().decode(encodedText);
            return new String(bytes, charset);
        } catch (UnsupportedEncodingException e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }

    private String decodeBEncodedText(String charset, String encodedText)
            throws ParseException {
        try {
            byte[] bytes = DatatypeConverter.parseBase64Binary(encodedText);
            return new String(bytes, charset);
        } catch (IllegalArgumentException | UnsupportedEncodingException e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }

    enum TokenKind {
        EQUAL, QUESTION, ATOKEN, EOF, ERROR,

        /**
         * This special token can only be returned by
         * {@link Scanner#scanEncodedText()}
         */
        ENCODED_TEXT,
    }

    private static class Token extends AbstractToken {
        public TokenKind kind;

        @Override
        protected String getKindAsString() {
            return kind.toString();
        }
    }

    private static class Scanner {
        private InputStream in;

        private int currentChar;
        private StringBuilder currentSpelling = new StringBuilder();
        private int position;

        public Scanner(InputStream in) {
            this.in = in;

            try {
                currentChar = in.read();
            } catch (IOException e) {
                // Fields are in memory, IOException is not expected.
                throw new RuntimeException(e);
            }
        }

        /**
         * Scans a usual token.
         * 
         * @return A token of type {@link TokenKind#ATOM}...
         */
        public Token scan() {
            Token token = new Token();
            token.position = position;
            currentSpelling.setLength(0);

            TokenKind currentKind = scanToken();

            token.kind = currentKind;
            token.spelling = currentSpelling.toString();
            return token;
        }

        private TokenKind scanToken() {
            if (currentChar == '=') {
                takeIt();
                return EQUAL;
            } else if (currentChar == '?') {
                takeIt();
                return QUESTION;
            } else if (starterAtoken()) {
                scanAtoken();
                return ATOKEN;
            } else if (currentChar == -1) {
                takeIt();
                return EOF;
            } else {
                return ERROR;
            }
        }

        private void scanAtoken() {
            take("atoken", isAtokenChar());
            while (isAtokenChar()) {
                takeIt();
            }
        }

        private boolean starterAtoken() {
            return isAtokenChar();
        }

        private boolean isAtokenChar() {
            if (currentChar < 0 || currentChar > 127)
                return false;
            if (currentChar == ' ')
                return false;
            if (isCtl())
                return false;
            if (isEspecial())
                return false;
            return true;
        }

        private boolean isCtl() {
            if (0 <= currentChar && currentChar <= 31)
                return true;
            if (currentChar == 127)
                return true;
            return false;
        }

        private boolean isEspecial() {
            switch (currentChar) {
            case '(':
            case ')':
            case '<':
            case '>':
            case '@':
            case ',':
            case ';':
            case ':':
            case '\\':
            case '"':
            case '/':
            case '[':
            case ']':
            case '?':
            case '.':
            case '=':
                return true;
            default:
                return false;
            }
        }

        public Token scanEncodedText() {
            Token token = new Token();
            token.position = position;
            currentSpelling.setLength(0);

            TokenKind currentKind = scanEncodedTextToken();

            token.kind = currentKind;
            token.spelling = currentSpelling.toString();
            return token;
        }

        private TokenKind scanEncodedTextToken() {
            take("encoded-text", isEncodedText());
            while (isEncodedText())
                takeIt();
            return ENCODED_TEXT;
        }

        private boolean isEncodedText() {
            if (!isPrintable())
                return false;
            if (currentChar == '?')
                return false;
            // space is also mentioned in RFC 2047, but that is already excluded
            // by isPrintable.
            return true;
        }

        private boolean isPrintable() {
            return 33 <= currentChar && currentChar <= 126;
        }

        private void takeIt() {
            try {
                if (currentChar != -1)
                    currentSpelling.append((char) currentChar);
                currentChar = in.read();
                position++;
            } catch (IOException e) {
                // fields are coming from memory, no IOException can occur
                throw new RuntimeException(e);
            }
        }

        private void take(String terminalName, boolean valid) {
            if (valid)
                takeIt();
            else
                throw new RuntimeException("Expected: " + terminalName
                        + " at position " + position); // TODO
        }

    }
}
