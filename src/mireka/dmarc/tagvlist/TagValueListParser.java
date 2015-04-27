package mireka.dmarc.tagvlist;

import static mireka.dmarc.tagvlist.TagValueListParser.TokenKind.*;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import mireka.maildata.AbstractToken;
import mireka.util.CharsetUtil;

public class TagValueListParser {
    private Token currentToken;
    private TagValueListScanner tagValueListScanner;
    private Scanner scanner;

    public TagValueList parse(String source) throws ParseException {
        String unfolded = new Unfolder().unfold(source);
        scanner = tagValueListScanner = new TagValueListScanner(unfolded);
        currentToken = scanner.scan();

        TagValueList tagValueList = new TagValueList();
        List<TagSpec> tagList = parseTagList();
        tagValueList.addAll(tagList);
        return tagValueList;
    }

    /**
     * <pre>
     *    tag-list  =  tag-spec *( ";" tag-spec ) [ ";" ]
     * </pre>
     */
    private List<TagSpec> parseTagList() throws ParseException {
        List<TagSpec> result = new ArrayList<>();

        TagSpec tagSpec = parseTagSpec();
        result.add(tagSpec);

        while (currentToken.kind == SEMICOLON) {
            acceptIt();

            if (starterTagSpec()) {
                tagSpec = parseTagSpec();
                result.add(tagSpec);
            } else {
                break;
            }
        }
        accept(EOF);

        return result;
    }

    private boolean starterTagSpec() {
        return currentToken.kind == NAME;
    }

    /**
     * <pre>
     *    tag-spec  =  [FWS] tag-name [FWS] "=" [FWS] tag-value [FWS]
     *    tag-name  =  ALPHA *ALNUMPUNC
     *    tag-value =  [ tval *( 1*(WSP / FWS) tval ) ]
     *                      ; Prohibits WSP and FWS at beginning and end
     * </pre>
     */
    private TagSpec parseTagSpec() throws ParseException {
        TagSpec result = new TagSpec();

        result.name = currentToken.spelling;
        accept(NAME);
        acceptButDontScanNextToken(EQUAL);
        result.value = parseTagValue();

        return result;
    }

    /**
     * It assumes that the currentToken is an old one, which is already
     * processed, so it can switch to TvalScanner.
     * 
     * <pre>
     * tag-value =  [ tval *( 1*(WSP / FWS) tval ) ]
     *                   ; Prohibits WSP and FWS at beginning and end
     * tval      =  1*VALCHAR
     * </pre>
     */
    private String parseTagValue() throws ParseException {
        scanner = tagValueListScanner.new TvalScanner();
        currentToken = scanner.scan();
        StringBuilder value = new StringBuilder();

        value.append(currentToken.spelling);
        accept(TVAL);

        while (currentToken.kind == TVAL) {
            value.append(currentToken.wspPrefix);
            value.append(currentToken.spelling);
            acceptIt();
        }

        acceptButDontScanNextToken(EMPTY);
        scanner = tagValueListScanner;
        currentToken = scanner.scan();

        return value.toString();
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

    private interface Scanner {
        Token scan();
    }

    private static class TagValueListScanner implements Scanner {
        private ByteArrayInputStream in;

        private int currentChar;
        private StringBuilder currentSpelling = new StringBuilder();
        /**
         * The index of currentChar in {@link #inputBytes}.
         */
        private int position;

        public TagValueListScanner(String input) {
            byte[] inputBytes = CharsetUtil.toAsciiBytes(input);
            this.in = new ByteArrayInputStream(inputBytes);

            currentChar = in.read();
        }

        public Token scan() {
            Token token = new Token();

            if (isWSP()) {
                currentSpelling.setLength(0);
                scanFWS();
                token.wspPrefix = currentSpelling.toString();
            } else {
                token.wspPrefix = "";
            }

            currentSpelling.setLength(0);
            token.position = position;

            TokenKind kind = scanToken();

            token.kind = kind;
            token.spelling = currentSpelling.toString();
            return token;
        }

        private boolean isWSP() {
            return currentChar == ' ' || currentChar == '\t';
        }

        private void scanFWS() {
            takeIt();
            while (isWSP())
                takeIt();
        }

        private TokenKind scanToken() {
            if (currentChar == ';') {
                takeIt();
                return SEMICOLON;
            } else if (currentChar == '=') {
                takeIt();
                return EQUAL;
            } else if (isAlpha()) {
                scanName();
                return NAME;
            } else if (currentChar == -1) {
                return EOF;
            } else {
                return ERROR;
            }
        }

        private void scanName() {
            takeIt();
            while (isAlNumPunc()) {
                takeIt();
            }
        }

        private boolean isAlNumPunc() {
            if (isAlpha())
                return true;
            if (isDigit())
                return true;
            if (currentChar == '_')
                return true;
            return false;
        }

        private boolean isDigit() {
            return '0' <= currentChar && currentChar <= '9';
        }

        private boolean isAlpha() {
            if (0x41 <= currentChar && currentChar <= 0x5A)
                return true;
            if (0x61 <= currentChar && currentChar <= 0x7A)
                return true;
            return false;
        }

        private void takeIt() {
            if (currentChar != -1)
                currentSpelling.append((char) currentChar);
            currentChar = in.read();
            position++;
        }

        public class TvalScanner implements Scanner {
            /**
             * Scans a tval and the prepending FWS if that exists.
             * 
             * <pre>
             * tval = 1 * VALCHAR
             * </pre>
             */
            public Token scan() {
                Token token = new Token();

                if (isWSP()) {
                    currentSpelling.setLength(0);
                    scanFWS();
                    token.wspPrefix = currentSpelling.toString();
                } else {
                    token.wspPrefix = "";
                }

                currentSpelling.setLength(0);
                token.position = position;

                TokenKind kind = scanToken();

                token.kind = kind;
                token.spelling = currentSpelling.toString();
                return token;
            }

            private TokenKind scanToken() {
                if (isValchar()) {
                    scanTval();
                    return TVAL;
                } else {
                    return EMPTY;
                }
            }

            private void scanTval() {
                takeIt();
                while (isValchar()) {
                    takeIt();
                }
            }

            /**
             * <pre>
             * VALCHAR   =  %x21-3A / %x3C-7E
             *                    ; EXCLAMATION to TILDE except SEMICOLON
             * </pre>
             */
            private boolean isValchar() {
                if (0x21 <= currentChar && currentChar <= 0x3A)
                    return true;
                if (0x3C <= currentChar && currentChar <= 0x7E)
                    return true;
                return false;
            }
        }

    }

    enum TokenKind {
        NAME, SEMICOLON, EQUAL, EOF, ERROR,
        /**
         * This is only returned by TvalScanner
         */
        TVAL,
        /**
         * This is only returned by TvalScanner, it means that no further tval
         * could be read.
         */
        EMPTY
    }

    private static class Token extends AbstractToken {
        private TokenKind kind;
        public String wspPrefix;

        @Override
        protected String getKindAsString() {
            return kind.toString();
        }

    }
}