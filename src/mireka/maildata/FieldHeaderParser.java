package mireka.maildata;

import static mireka.maildata.FieldHeaderParser.TokenKind.*;

import java.io.ByteArrayInputStream;
import java.text.ParseException;

import mireka.util.CharsetUtil;

/**
 * FieldHeaderParser creates the top level overview of the field by determining
 * the name of the field and the starting position of the field body.
 */
public class FieldHeaderParser {
    private Token currentToken;
    private Scanner scanner;

    public FieldHeaderParser(String unfoldedField) {
        scanner = new Scanner(unfoldedField);
        currentToken = scanner.scan();
    }

    public FieldMap parse() throws ParseException {

        String name = parseNamePart();

        FieldMap result = new FieldMap();
        result.name = name;
        result.indexOfBody = scanner.position;
        return result;
    }

    /**
     * Returns the name of the field. The last scanned token is the COLON, which
     * follows the field name (and the optional WSP characters).
     */
    private String parseNamePart() throws ParseException {
        String name = currentToken.spelling;
        accept(NAME);
        if (currentToken.kind == LWSP)
            acceptIt();
        acceptButDontScanNextToken(COLON);
        return name;
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

    public class Scanner {
        private ByteArrayInputStream in;

        private int currentChar;
        private StringBuilder currentSpelling = new StringBuilder();
        /**
         * The index of currentChar in {@link #inputBytes}.
         */
        private int position;

        public Scanner(String input) {
            byte[] inputBytes = CharsetUtil.toAsciiBytes(input);
            this.in = new ByteArrayInputStream(inputBytes);

            currentChar = in.read();
        }

        public Token scan() {
            Token token = new Token();
            token.position = position;
            currentSpelling.setLength(0);
            TokenKind currentKind;

            if (currentChar == ':') {
                takeIt();
                currentKind = COLON;
            } else if (isLWSP()) {
                scanLWSP();
                currentKind = LWSP;
            } else if (isFtext()) {
                scanFtext();
                currentKind = NAME;
            } else {
                currentKind = ERROR;
            }

            token.kind = currentKind;
            token.spelling = currentSpelling.toString();
            return token;
        }

        private boolean isLWSP() {
            // Input is already unfolded
            return currentChar == ' ' || currentChar == '\t';
        }

        private void scanLWSP() {
            // Input is already unfolded
            while (isLWSP()) {
                takeIt();
            }
        }

        /**
         * Printable US-ASCII characters not including ":".
         */
        private boolean isFtext() {
            if (33 <= currentChar && currentChar <= 57)
                return true;
            if (59 <= currentChar && currentChar <= 126)
                return true;
            return false;
        }

        private void scanFtext() {
            takeIt();
            while (isFtext()) {
                takeIt();
            }
        }

        private void takeIt() {
            if (currentChar != -1)
                currentSpelling.append((char) currentChar);
            currentChar = in.read();
            position++;
        }
    }

    enum TokenKind {
        NAME, LWSP, COLON, ERROR;
    }

    private static class Token extends AbstractToken {
        public TokenKind kind;

        @Override
        protected String getKindAsString() {
            return kind.toString();
        }

    }

    public static class FieldMap {
        String name;
        int indexOfBody;
    }

}
