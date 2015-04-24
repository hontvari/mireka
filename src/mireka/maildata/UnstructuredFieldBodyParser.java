package mireka.maildata;

import static mireka.maildata.UnstructuredFieldBodyParser.TokenKind.*;

import java.io.ByteArrayInputStream;
import java.text.ParseException;

import mireka.maildata.field.UnstructuredField;
import mireka.util.CharsetUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnstructuredFieldBodyParser {
    private final Logger logger = LoggerFactory
            .getLogger(UnstructuredFieldBodyParser.class);

    private Token currentToken;
    private Scanner scanner;

    public UnstructuredFieldBodyParser(String body) {
        scanner = new Scanner(body);
        currentToken = scanner.scan();
    }

    public UnstructuredField parse() throws ParseException {
        String body = parseBody();

        UnstructuredField result = new UnstructuredField();
        result.body = body.toString();
        return result;
    }

    private String parseBody() throws ParseException {
        StringBuilder body = new StringBuilder();

        while (currentToken.kind == LWSP || currentToken.kind == WORD) {
            switch (currentToken.kind) {
            case LWSP:
                body.append(currentToken.spelling);
                acceptIt();
                break;
            case WORD:
                if (starterEncodedWordInUnstructured()) {
                    String decodedText =
                            parseEncodedWordSequenceInUnstructured();
                    body.append(decodedText);
                } else {
                    body.append(currentToken.spelling);
                    acceptIt();
                }
                break;
            default:
                throw new RuntimeException("Assertion failed");
            }
        }
        accept(EOF);
        return body.toString();
    }

    private boolean starterEncodedWordInUnstructured() {
        return currentToken.kind == WORD
                && EncodedWordParser.isEncodedWord(currentToken.spelling);
    }

    /**
     * <pre>
     * encoded-word-sequence = encoded-word *(LWSP encoded-word) [LWSP]
     * </pre>
     * 
     * The first LWSP is not, but the last, optional LWSP is part of the
     * semantic content.
     */
    private String parseEncodedWordSequenceInUnstructured() {
        // semantic content
        StringBuilder semanticContent = new StringBuilder();

        String content = parseEncodedWordInUnstructured();
        semanticContent.append(content);

        Token unprocessedLWSPToken = null;
        while (currentToken.kind == LWSP) {
            unprocessedLWSPToken = currentToken;
            acceptIt();
            if (starterEncodedWordInUnstructured()) {
                // LWSP between two encoded-word is not part of the semantic
                // content.
                unprocessedLWSPToken = null;
                content = parseEncodedWordInUnstructured();
                semanticContent.append(content);
            } else {
                break;
            }
        }
        if (unprocessedLWSPToken != null)
            semanticContent.append(unprocessedLWSPToken.spelling);

        return semanticContent.toString();
    }

    private String parseEncodedWordInUnstructured() {
        if (currentToken.kind != WORD)
            throw new RuntimeException("Assertion failed");

        String result;
        try {
            result = new EncodedWordParser().parse(currentToken.spelling);
        } catch (ParseException e) {
            logger.debug("encoded-word cannot be parsed, using it as is. '"
                    + currentToken.spelling + "'", e);
            result = currentToken.spelling;
        }
        acceptIt();
        return result;
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

    private static class Scanner {
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
            TokenKind tokenKind;

            if (isLWSP()) {
                scanLWSP();
                tokenKind = LWSP;
            } else if (isEOF()) {
                tokenKind = EOF;
            } else if (isUtext()) {
                scanWord();
                tokenKind = WORD;
            } else {
                throw new RuntimeException("Assertion failed");
            }

            token.kind = tokenKind;
            token.spelling = currentSpelling.toString();
            return token;
        }

        private boolean isLWSP() {
            return currentChar == ' ' || currentChar == '\t';
        }

        /**
         * Input is already unfolded
         */
        private void scanLWSP() {
            while (isLWSP()) {
                takeIt();
            }
        }

        private boolean isEOF() {
            return currentChar == -1;
        }

        private void scanWord() {
            while (isUtext()) {
                takeIt();
            }
        }

        private boolean isUtext() {
            if (currentChar < 0)
                return false;
            if (currentChar > 127)
                return false;
            switch (currentChar) {
            case ' ':
            case '\t':
                return false;
            default:
                return true;
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
        /** end of input **/
        EOF,
        /**
         * This special token kind is returned if the source text is
         * syntactically invalid. Using this object instead of throwing an
         * exception results in a better error message.
         */
        ERROR,

        /**
         * Sequence of white space characters.
         */
        LWSP,
        /**
         * Sequence of non-whitespace characters, e.g letters.
         */
        WORD,
    }

    private static class Token extends AbstractToken {
        public TokenKind kind;

        @Override
        protected String getKindAsString() {
            return kind.toString();
        }

    }

}
