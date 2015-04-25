package mireka.dmarc;

import static mireka.dmarc.Unfolder.TokenKind.*;

import java.io.ByteArrayInputStream;
import java.text.ParseException;

import mireka.maildata.AbstractToken;
import mireka.util.CharsetUtil;

public class Unfolder {
    private Token currentToken;
    private Scanner scanner;

    /**
     * Returns the input unfolded, that is any CRLF before a WSP is removed.
     *
     * The grammar used by this operation:
     * 
     * <pre>
     * folded = *(utext / WSP) *(CRLF WSP *(utext / WSP))
     * utext = %x21-7E
     *         ; any visible ASCII character (no control, no CR, LF, WSP)
     * </pre>
     * 
     * CRLF is not part of the semantic content, anything other is.
     */
    public String unfold(String source) throws ParseException {
        scanner = new Scanner(source);
        currentToken = scanner.scan();
        StringBuilder unfolded = new StringBuilder(1024);

        while (currentToken.kind == UTEXT || currentToken.kind == WSP) {
            unfolded.append(currentToken.spelling);
            acceptIt();
        }

        while (currentToken.kind == CRLF) {
            acceptIt();
            unfolded.append(currentToken.spelling);
            accept(WSP);

            while (currentToken.kind == UTEXT || currentToken.kind == WSP) {
                unfolded.append(currentToken.spelling);
                acceptIt();
            }
        }

        accept(EOF);

        return unfolded.toString();
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

    private class Scanner {
        private ByteArrayInputStream in;
        private int currentChar;
        private int position;
        private StringBuilder currentSpelling = new StringBuilder();

        public Scanner(String source) {
            byte[] inputBytes = CharsetUtil.toAsciiBytes(source);
            this.in = new ByteArrayInputStream(inputBytes);

            currentChar = in.read();
        }

        public Token scan() {
            currentSpelling.setLength(0);
            Token token = new Token();
            token.position = position;

            token.kind = scanToken();
            token.spelling = currentSpelling.toString();
            return token;
        }

        private TokenKind scanToken() {
            if (0x21 <= currentChar && currentChar <= 0x7E) {
                takeIt();
                return UTEXT;
            }

            switch (currentChar) {
            case '\r':
                takeIt();
                if (currentChar == '\n') {
                    takeIt();
                    return CRLF;
                } else {
                    return ERROR;
                }
            case ' ':
            case '\t':
                takeIt();
                return WSP;
            case -1:
                return EOF;
            default:
                return ERROR;
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
        /**
         * CR LF
         */
        CRLF,
        /**
         * Printable ASCII characters
         */
        UTEXT,
        /**
         * Whitespace, either Tab or Space
         */
        WSP,
        /**
         * End of input
         */
        EOF,
        /**
         * The ERROR token indicates a syntax error, in case of this scanner
         * this means an illegal character.
         */
        ERROR
    };

    private class Token extends AbstractToken {
        public TokenKind kind;

        @Override
        protected String getKindAsString() {
            return kind.toString();
        }

    }

}
