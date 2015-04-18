package mireka.maildata;

import static mireka.maildata.FieldParser.TokenKind.*;
import static mireka.util.CharsetUtil.*;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import mireka.util.CharsetUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FieldParser {
    private final Logger logger = LoggerFactory.getLogger(FieldParser.class);

    private Token currentToken;
    private Scanner scanner;

    public HeaderField parseField(String unfoldedField) throws ParseException {
        this.scanner = new Scanner(unfoldedField);
        return parseField();
    }

    private HeaderField parseField() throws ParseException {
        currentToken = scanner.scanFieldName();
        String name = toAsciiLowerCase(currentToken.semanticContent);

        switch (name) {
        case "from":
            return parseFromField();
        default:
            return parseUnstructuredField();
        }
    }

    /**
     * <pre>
     * obs-from        =   "From" *WSP ":" mailbox-list CRLF
     * </pre>
     */
    private FromHeader parseFromField() throws ParseException {
        FromHeader result = new FromHeader();

        result.setName(currentToken.semanticContent);
        acceptIt();
        result.mailboxList = parseMailboxList();
        accept(CRLF);

        return result;
    }

    private UnstructuredHeader parseUnstructuredField() throws ParseException {
        UnstructuredHeader result = new UnstructuredHeader();

        result.setName(currentToken.semanticContent);
        currentToken = scanner.scanUnstruct();
        result.body = currentToken.semanticContent;
        accept(UNSTRUCT);

        return result;
    }

    /**
     * <pre>
     * obs-mbox-list   =   *([CFWS] ",") mailbox *("," [mailbox / CFWS])
     * </pre>
     */
    private List<Mailbox> parseMailboxList() throws ParseException {
        List<Mailbox> result = new ArrayList<>();
        Mailbox mailbox;

        while (currentToken.kind == COMMA) {
            acceptIt();
        }

        mailbox = parseMailbox();
        result.add(mailbox);

        while (currentToken.kind == COMMA) {
            acceptIt();
            if (starterMailbox()) {
                mailbox = parseMailbox();
                result.add(mailbox);
            }
        }

        return result;
    }

    /**
     * <pre>
     * mailbox         =   name-addr / addr-spec
     * 
     * name-addr       =   [display-name] angle-addr
     * 
     * display-name    =   phrase
     * 
     * phrase          =   1*word / obs-phrase
     * 
     * obs-phrase      =   word *(word / "." / CFWS)
     * </pre>
     */
    private Mailbox parseMailbox() throws ParseException {
        Mailbox result = new Mailbox();

        if (currentToken.kind == LESS_THEN) {
            result.addrSpec = parseAngleAddr();
        } else if (isWord()) {
            // Either phrase (display-name) or the local-part of addr-spec
            // starts here.
            // In a phrase whitespace is semantically visible, in addr-spec it
            // is not.
            switch (lookAheadOverDisplayNameOrLocalPartInMailbox()) {
            case LOCAL_PART:
                result.addrSpec = new AddrSpec();
                result.addrSpec.localPart = parseLocalPart();
                accept(AT);
                result.addrSpec.domain = parseDomain();
                break;
            case DISPLAY_NAME:
                result.displayName = parsePhrase();
                result.addrSpec = parseAngleAddr();
                break;
            }
        } else {
            throw currentToken.syntaxException("mailbox");
        }
        return result;
    }

    /**
     * Determines which mailbox alternative matches the input, if it starts with
     * a display-name or an addr-spec. It assumes that the angle-address without
     * display-name rule is already excluded.
     * 
     * @throws ParseException
     */
    private MailboxAlternative lookAheadOverDisplayNameOrLocalPartInMailbox()
            throws ParseException {
        if (!isWord())
            throw new IllegalStateException();

        MailboxAlternative result;
        Token originalToken = currentToken;
        Scanner originalScanner = scanner;
        scanner = scanner.getLookaheadScanner();

        parseWord();
        while (isWord() || currentToken.kind == PERIOD) {
            if (isWord()) {
                parseWord();
            } else if (currentToken.kind == PERIOD) {
                acceptIt();
            } else {
                throw new RuntimeException();
            }
        }

        if (currentToken.kind == AT) {
            result = MailboxAlternative.LOCAL_PART;
        } else if (currentToken.kind == LESS_THEN) {
            result = MailboxAlternative.DISPLAY_NAME;
        } else {
            throw currentToken.syntaxException("addr-spec or angle-addr");
        }

        scanner = originalScanner;
        currentToken = originalToken;
        return result;
    }

    // @formatter:off (Eclipse formatter inserts spaces around hyphen in encoded-word) 
    /**
     * Parses a 'phrase', which is basically a sequence of words, with some
     * extra.
     *
     * The 'phrase' is a subset of obs-phrase, so consider only the latter.
     *
     * <pre>
     * obs-phrase      =   word *(word / "." / CFWS)
     * </pre>
     *
     * Dot is also allowed. In addition, in a phrase whitespace is semantically
     * visible, in contrast to for example addr-spec. Series of whitespace
     * characters between words are semantically equivalent of a single space
     * character.
     *
     * RFC 2047 further enhances this rule:
     *
     * <pre>
     * phrase = 1 * (encoded-word / word)
     * </pre>
     *
     * An additional rule is that the space between two encoded-words are 
     * semantically invisible.
     *
     * All in all a complete rule in EBNF:
     * <pre>
     * phrase = phrase-word *(phrase-word / "." / CFWS)
     * phrase-word = encoded-word-sequence / word
     * encoded-word-sequence = 1 * encoded-word
     * </pre>
     */
    // @formatter:on
    private String parsePhrase() throws ParseException {
        StringBuilder buffer = new StringBuilder();
        String content = parsePhraseWord();
        buffer.append(content);

        while (starterPhraseWord() || currentToken.kind == PERIOD) {
            if (starterPhraseWord()) {
                buffer.append(currentToken.collapsedWhitespace);
                content = parsePhraseWord();
                buffer.append(content);
            } else if (currentToken.kind == PERIOD) {
                buffer.append(currentToken.getSemanticContentWithWsPrefix());
                acceptIt();
            } else {
                throw new RuntimeException();
            }
        }

        return buffer.toString();
    }

    private boolean starterPhraseWord() {
        return isWord();
    }

    private String parsePhraseWord() throws ParseException {
        StringBuilder buffer = new StringBuilder();

        if (currentToken.kind == QUOTED_STRING) {
            buffer.append(currentToken.semanticContent);
            acceptIt();
        } else if (starterEncodedWord()) {
            String content = parseEncodedWordSequence();
            buffer.append(content);
        } else if (isWord()) {
            buffer.append(currentToken.semanticContent);
            acceptIt();
        } else {
            throw currentToken
                    .syntaxException("ATOM including encoded-words or QUOTED_STRING");
        }
        return buffer.toString();
    }

    private String parseEncodedWordSequence() {
        // semantic content
        StringBuilder semanticContent = new StringBuilder();

        String content = parseEncodedWord();
        semanticContent.append(content);

        while (starterEncodedWord()) {
            content = parseEncodedWord();
            semanticContent.append(content);
        }

        return semanticContent.toString();
    }

    private boolean starterEncodedWord() {
        return currentToken.kind == ATOM
                && EncodedWordParser
                        .isEncodedWord(currentToken.semanticContent);
    }

    private String parseEncodedWord() {
        if (currentToken.kind != ATOM)
            throw new RuntimeException("Assertion failed");

        String result;
        try {
            result =
                    new EncodedWordParser().parse(currentToken.semanticContent);
        } catch (ParseException e) {
            logger.debug("encoded-word cannot be parsed, using it as is. '"
                    + currentToken.semanticContent + "'", e);
            result = currentToken.semanticContent;
        }
        acceptIt();
        return result;
    }

    private boolean starterMailbox() {
        return currentToken.kind == LESS_THEN || isWord();
    }

    /**
     * <pre>
     * 
     * [CFWS] "<" obs-route addr-spec ">" [CFWS]
     * 
     * obs-route       =   obs-domain-list ":"
     * 
     * obs-domain-list =   *(CFWS / ",") "@" domain
     *                     *("," [CFWS] ["@" domain])
     * </pre>
     */
    private AddrSpec parseAngleAddr() throws ParseException {
        AddrSpec result;

        accept(LESS_THEN);
        if (currentToken.kind == COMMA || currentToken.kind == AT) {
            parseRoute();
        }
        result = parseAddrSpec();
        accept(GREATER_THEN);

        return result;
    }

    /**
     * <pre>
     * obs-route       =   obs-domain-list ":"
     * </pre>
     */
    private void parseRoute() throws ParseException {
        parseDomainList();
        accept(COLON);
    }

    /**
     * <pre>
     * obs-domain-list =   *(CFWS / ",") "@" domain
     *                     *("," [CFWS] ["@" domain])
     * </pre>
     */
    private void parseDomainList() throws ParseException {
        while (currentToken.kind == COMMA)
            acceptIt();
        accept(AT);
        parseDomain();
        while (currentToken.kind == COMMA) {
            acceptIt();
            if (currentToken.kind == AT) {
                acceptIt();
                parseDomain();
            }
        }
    }

    /**
     * Returns the semantic content of the domain.
     * 
     * <pre>
     * domain          =   dot-atom / domain-literal / obs-domain
     * obs-domain      =   atom *("." atom)
     * </pre>
     */
    private DomainPart parseDomain() throws ParseException {
        if (currentToken.kind == ATOM) {
            return parseObsDomain();
        } else if (currentToken.kind == LEFT_S_BRACKET) {
            return parseDomainLiteral();
        } else {
            throw currentToken.syntaxException("domain");
        }
    }

    private DotAtomDomainPart parseObsDomain() throws ParseException {
        StringBuilder result = new StringBuilder();

        result.append(currentToken.semanticContent);
        accept(ATOM);
        while (currentToken.kind == PERIOD) {
            result.append(currentToken.semanticContent);
            acceptIt();
            result.append(currentToken.semanticContent);
            accept(ATOM);
        }

        return new DotAtomDomainPart(result.toString());
    }

    private LiteralDomainPart parseDomainLiteral() throws ParseException {
        StringBuilder result = new StringBuilder();
        acceptButDontScanNextToken(LEFT_S_BRACKET);

        currentToken = scanner.scanDtextString();
        result.append(currentToken.semanticContent);
        acceptIt();

        accept(RIGHT_S_BRACKET);

        return new LiteralDomainPart(result.toString());
    }

    public AddrSpec parseAddrSpec(String emailAddress) throws ParseException {
        this.scanner = new Scanner(emailAddress);
        currentToken = scanner.scan();
        return parseAddrSpec();
    }

    /**
     * <pre>
     * addr-spec       =   local-part "@" domain
     * </pre>
     */
    private AddrSpec parseAddrSpec() throws ParseException {
        AddrSpec result = new AddrSpec();

        result.localPart = parseLocalPart();
        accept(AT);
        result.domain = parseDomain();

        return result;
    }

    /**
     * Returns the semantic value of the local-part.
     * 
     * <pre>
     * obs-local-part  =   word *("." word)
     * </pre>
     */
    private String parseLocalPart() throws ParseException {
        StringBuilder result = new StringBuilder();

        Token word = parseWord();
        result.append(word.semanticContent);
        while (currentToken.kind == PERIOD) {
            acceptIt();
            word = parseWord();
            result.append(word.semanticContent);
        }

        return result.toString();
    }

    /**
     * Parses a word which is either an ATOM or a QUOTED_STRING.
     * 
     * <pre>
     * word = atom / quoted - string
     * </pre>
     * 
     * @return the token which was the content of the word, either an ATOM or a
     *         QUOTED_STRING.
     */
    private Token parseWord() throws ParseException {
        Token wordToken = currentToken;
        if (currentToken.kind == ATOM) {
            acceptIt();
        } else if (currentToken.kind == QUOTED_STRING) {
            acceptIt();
        } else {
            throw currentToken.syntaxException("word");
        }
        return wordToken;
    }

    /**
     * Returns true if the current token "starts" a 'word' non-terminal. It is
     * worth noting that a word consists of a single token, so the current token
     * is the sole content of that word.
     */
    private boolean isWord() {
        return currentToken.kind == ATOM || currentToken.kind == QUOTED_STRING;
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

    private static class Scanner {
        private byte[] inputBytes;
        private ByteArrayInputStream in;

        private int currentChar;
        private StringBuilder currentSpelling = new StringBuilder();
        private StringBuilder currentSemContent = new StringBuilder();
        /**
         * The index of currentChar in {@link #inputBytes}.
         */
        private int position;

        public Scanner(String input) {
            this.inputBytes = CharsetUtil.toAsciiBytes(input);
            this.in = new ByteArrayInputStream(inputBytes);

            currentChar = in.read();
        }

        /**
         * Copy constructor. Deep copy, except the inputBytes array.
         */
        private Scanner(Scanner original) {
            inputBytes = original.inputBytes;
            position = original.position;
            in =
                    new ByteArrayInputStream(inputBytes, position + 1,
                            inputBytes.length - position - 1);
            currentChar = original.currentChar;
            currentSpelling = new StringBuilder(original.currentSpelling);
            currentSemContent = new StringBuilder(original.currentSemContent);
        }

        /**
         * Scans a usual token.
         * 
         * @return A token of type {@link TokenKind#ATOM}...
         */
        public Token scan() {
            String collapsedWhiteSpace = "";
            if (starterCFWS()) {
                scanCFWS();
                collapsedWhiteSpace = " ";
            }

            Token token = new Token();
            token.position = position;
            currentSpelling.setLength(0);
            currentSemContent.setLength(0);

            TokenKind currentKind = scanToken();

            token.kind = currentKind;
            token.spelling = currentSpelling.toString();
            token.collapsedWhitespace = collapsedWhiteSpace;
            token.semanticContent = currentSemContent.toString();
            return token;
        }

        /**
         * Returns true if the currentChar can be the start of a CFWS token.
         */
        private boolean starterCFWS() {
            return starterFWS() || starterComment();
        }

        /**
         * <pre>
         * CFWS            =   (1*([FWS] comment) [FWS]) / FWS
         * 
         * obs-FWS         =   1*WSP *(CRLF 1*WSP)
         * 
         * comment         =   "(" *([FWS] ccontent) [FWS] ")"
         * 
         * ccontent        =   ctext / quoted-pair / comment
         * 
         * ctext           =   %d33-39 /          ; Printable US-ASCII
         *                     %d42-91 /          ;  characters not including
         *                     %d93-126 /         ;  "(", ")", or "\"
         *                     obs-ctext
         * 
         * obs-ctext       =   obs-NO-WS-CTL
         * 
         * obs-NO-WS-CTL   =   %d1-8 /            ; US-ASCII control
         *                     %d11 /             ;  characters that do not
         *                     %d12 /             ;  include the carriage
         *                     %d14-31 /          ;  return, line feed, and
         *                     %d127              ;  white space characters
         * 
         * </pre>
         */
        private void scanCFWS() {
            while (starterFWS() || starterComment()) {
                if (starterFWS())
                    scanFWS();
                else
                    scanComment();
            }
        }

        private boolean starterFWS() {
            return currentChar == ' ' || currentChar == '\t';
        }

        private boolean starterComment() {
            return currentChar == '(';
        }

        /**
         * Input is already unfolded
         */
        private void scanFWS() {
            while (isWSP()) {
                // it can appear within a quoted-string where it is semantically
                // visible
                addToSemanticContent();
                takeIt();
            }
        }

        private boolean isWSP() {
            return currentChar == ' ' || currentChar == '\t';
        }

        private void scanComment() {
            take('(');
            while (starterFWS() || starterCContent()) {
                if (starterFWS()) {
                    scanFWS();
                } else {
                    scanCContent();
                }
            }
            take(')');
        }

        private boolean starterCContent() {
            return isCText() || starterQuotedPair() || starterComment();
        }

        private boolean isCText() {
            if (33 <= currentChar && currentChar <= 39)
                return true;
            if (42 <= currentChar && currentChar <= 91)
                return true;
            if (93 <= currentChar && currentChar <= 126)
                return true;
            if (isObsoleteNoWsCtl())
                return true;
            return false;
        }

        private boolean isObsoleteNoWsCtl() {
            if (1 <= currentChar && currentChar <= 8)
                return true;
            if (11 <= currentChar && currentChar <= 12)
                return true;
            if (14 <= currentChar && currentChar <= 31)
                return true;
            if (127 == currentChar)
                return true;
            return false;
        }

        private boolean starterQuotedPair() {
            return currentChar == '\\';
        }

        private void scanCContent() {
            if (isCText())
                takeIt();
            else if (starterQuotedPair()) {
                scanQuotedPair();
            } else if (starterComment()) {
                scanComment();
            } else {
                throw new RuntimeException("Assertion failed");
            }
        }

        private void scanQuotedPair() {
            take('\\');
            addToSemanticContent();
            takeIt();
        }

        private TokenKind scanToken() {
            if (starterAtom()) {
                scanAtom();
                return ATOM;
            } else if (currentChar == '"') {
                scanQuotedString();
                return QUOTED_STRING;
            } else if (currentChar == '\r') {
                takeIt();
                if (currentChar == '\n') {
                    takeIt();
                    return CRLF;
                } else {
                    // standalone CR character is invalid for this scanner
                    // method
                    return ERROR;
                }
            } else {
                switch (currentChar) {
                case '<':
                    addToSemanticContent();
                    takeIt();
                    return LESS_THEN;
                case '>':
                    addToSemanticContent();
                    takeIt();
                    return GREATER_THEN;
                case '[':
                    addToSemanticContent();
                    takeIt();
                    return LEFT_S_BRACKET;
                case ']':
                    addToSemanticContent();
                    takeIt();
                    return RIGHT_S_BRACKET;
                case ':':
                    addToSemanticContent();
                    takeIt();
                    return COLON;
                case '@':
                    addToSemanticContent();
                    takeIt();
                    return AT;
                case ',':
                    addToSemanticContent();
                    takeIt();
                    return COMMA;
                case '.':
                    addToSemanticContent();
                    takeIt();
                    return PERIOD;
                default:
                    return ERROR;
                }
            }
        }

        private boolean starterAtom() {
            return isAText();
        }

        private boolean isAText() {
            if (isAlpha() || isDigit())
                return true;
            switch (currentChar) {
            case '!':
            case '#':
            case '$':
            case '%':
            case '&':
            case '\'':
            case '*':
            case '+':
            case '-':
            case '/':
            case '=':
            case '?':
            case '^':
            case '_':
            case '`':
            case '{':
            case '|':
            case '}':
            case '~':
                return true;
            }
            return false;
        }

        private boolean isAlpha() {
            if (0x41 <= currentChar && currentChar <= 0x5A)
                return true;
            if (0x61 <= currentChar && currentChar <= 0x7A)
                return true;
            return false;
        }

        private boolean isDigit() {
            return 0x30 <= currentChar && currentChar <= 0x39;
        }

        private void scanAtom() {
            addToSemanticContent();
            take("atext", isAText());

            while (isAText()) {
                addToSemanticContent();
                takeIt();
            }
        }

        private void scanQuotedString() {
            take('"');
            while (starterFWS() || starterQcontent()) {
                if (starterFWS())
                    scanFWS();
                else
                    scanQcontent();
            }
            take('"');
        }

        private void scanQcontent() {
            if (isQtext()) {
                addToSemanticContent();
                takeIt();
            } else if (starterQuotedPair()) {
                scanQuotedPair();
            } else {
                throw new RuntimeException("Assertion failed");
            }
        }

        private boolean starterQcontent() {
            return isQtext() || starterQuotedPair();
        }

        private boolean isQtext() {
            if (33 == currentChar)
                return true;
            if (35 <= currentChar && currentChar <= 91)
                return true;
            if (93 <= currentChar && currentChar <= 126)
                return true;
            if (isObsoleteQtext())
                return true;
            return false;
        }

        private boolean isObsoleteQtext() {
            return isObsoleteNoWsCtl();
        }

        /**
         * Scans a header field name using special syntax, including the
         * separator colon.
         * 
         * @return A TokenKind.FIELD_NAME type token
         */
        public Token scanFieldName() {
            Token token = new Token();
            token.position = position;
            currentSpelling.setLength(0);
            currentSemContent.setLength(0);

            addToSemanticContent();
            take("ftext", isFtext());
            while (isFtext()) {
                addToSemanticContent();
                takeIt();
            }

            while (isWSP())
                takeIt();
            take(':');

            token.kind = FIELD_NAME;
            token.spelling = currentSpelling.toString();
            token.collapsedWhitespace = "";
            token.semanticContent = currentSemContent.toString();
            return token;
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

        /**
         * Scans a dtext-string. Note that no dtext-string token is defined
         * explicitly in RFC5322. It is the inner side of a domain-literal,
         * within the square brackets.
         * 
         * <pre>
         * dtext-string    =   *([FWS] dtext) [FWS]
         * 
         * obs-dtext       =   obs-NO-WS-CTL / quoted-pair
         * </pre>
         */
        public Token scanDtextString() {
            Token token = new Token();
            token.position = position;
            currentSpelling.setLength(0);
            currentSemContent.setLength(0);

            while (starterFWS() || isDtext() || starterQuotedPair()) {
                if (starterFWS()) {
                    scanFWS();
                } else if (isDtext()) {
                    addToSemanticContent();
                    takeIt();
                } else if (starterQuotedPair()) {
                    scanQuotedPair();
                } else {
                    throw new RuntimeException();
                }
            }

            token.kind = DTEXT;
            token.spelling = currentSpelling.toString();
            token.collapsedWhitespace = "";
            token.semanticContent = currentSemContent.toString();
            return token;
        }

        private boolean isDtext() {
            if (33 <= currentChar && currentChar <= 90)
                return true;
            if (94 <= currentChar && currentChar <= 126)
                return true;
            return isObsoleteNoWsCtl();
        }

        /**
         * Returns an UNSTRUCT token. It scans the ending CRLF too.
         */
        public Token scanUnstruct() {
            Token token = new Token();
            token.position = position;
            currentSpelling.setLength(0);
            currentSemContent.setLength(0);

            while (true) {
                if (currentChar == '\r') {
                    takeIt();
                    if (currentChar == '\n') {
                        takeIt();
                        break;
                    } else {
                        currentSemContent.append('\r');
                    }
                } else {
                    addToSemanticContent();
                    takeIt();
                }
            }

            token.kind = UNSTRUCT;
            token.spelling = currentSpelling.toString();
            token.collapsedWhitespace = "";
            token.semanticContent = currentSemContent.toString();
            return token;

        }

        private void takeIt() {
            if (currentChar != -1)
                currentSpelling.append((char) currentChar);
            currentChar = in.read();
            position++;
        }

        private void take(int c) {
            if (currentChar != c)
                throw new RuntimeException("Expected: " + c + " at position "
                        + position); // TODO
            takeIt();
        }

        private void take(String terminalName, boolean valid) {
            if (valid)
                takeIt();
            else
                throw new RuntimeException("Expected: " + terminalName
                        + " at position " + position); // TODO
        }

        private void addToSemanticContent() {
            currentSemContent.append((char) currentChar);
        }

        /**
         * Returns a copy of this scanner for lookahead. The new scanner does
         * not affect the current scanner in any way.
         */
        public Scanner getLookaheadScanner() {
            return new Scanner(this);
        }
    }

    enum TokenKind {
        ATOM, QUOTED_STRING, CRLF, ERROR,

/** '<' */
        LESS_THEN,
        /** '>' */
        GREATER_THEN,
        /** '[' */
        LEFT_S_BRACKET,
        /** ']' */
        RIGHT_S_BRACKET,
        /** ':' */
        COLON,
        /** '@' */
        AT,
        /** '\' */
        COMMA,
        /** '.' */
        PERIOD,

        /**
         * This special token can only be returned by
         * {@link Scanner#scanFieldName()}
         */
        FIELD_NAME,
        /**
         * This special token can only be returned by
         * {@link Scanner#scanDtext()}
         */
        DTEXT,
        /**
         * The semantic content is the entire line not including the ending
         * CRLF.
         * 
         * This special token can only be returned by
         * {@link Scanner#scanUnstruct()}
         */
        UNSTRUCT,
    }

    private static class Token extends AbstractToken {
        public TokenKind kind;
        /**
         * It contains either a single space character, if the token was
         * preceded by one or more white space (FWS, comment, CFWS) elements
         * used as separators, or an empty string.
         */
        public String collapsedWhitespace;
        /**
         * Spelling of the token without the semantically invisible characters,
         * e.g. the string within a quoted-string without the external CFWS and
         * double quotes.
         */
        public String semanticContent;

        public String getSemanticContentWithWsPrefix() {
            return collapsedWhitespace + semanticContent;
        }

        @Override
        protected String getKindAsString() {
            return kind.toString();
        }

    }

    /**
     * Result of an lookahead in a Mailbox nonterminal, where it is difficult to
     * select from the alternative rules without extra lookahead.
     */
    private enum MailboxAlternative {
        DISPLAY_NAME, LOCAL_PART
    }
}
