package mireka.sieve;

import static mireka.sieve.ComparatorKind.*;
import static mireka.sieve.Interpreter.CharClass.*;
import static mireka.sieve.Kind.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mireka.imap.CiString;
import mireka.sieve.ast.AddressPart;
import mireka.sieve.ast.AddressTc;
import mireka.sieve.ast.BiLogicalTc;
import mireka.sieve.ast.Block;
import mireka.sieve.ast.Command;
import mireka.sieve.ast.ComparatorType;
import mireka.sieve.ast.ConstantTc;
import mireka.sieve.ast.FileintoAction;
import mireka.sieve.ast.FlagAction;
import mireka.sieve.ast.HeaderTc;
import mireka.sieve.ast.IfControl;
import mireka.sieve.ast.KeepAction;
import mireka.sieve.ast.MatchType;
import mireka.sieve.ast.NotTc;
import mireka.sieve.ast.NullaryCommand;
import mireka.sieve.ast.Program;
import mireka.sieve.ast.RequireControl;
import mireka.sieve.ast.Testcommand;

public class Interpreter {
    private static final int EOF = -1;
    private static final int CRLF = -2;

    private final Logger logger = LoggerFactory.getLogger(Interpreter.class);
    private final PushbackInputStream in;
    private Scanner scanner;
    private Token next;
    private EnumSet<Capability> extensions = EnumSet.noneOf(Capability.class);
    /**
     * Enabled comparators
     */
    private EnumSet<ComparatorKind> comparators = EnumSet.of(OCTET, ASCII_CASEMAP);

    public static void main(String[] args) throws Exception {
        Context callback = new Context();
        Interpreter interpreter = new Interpreter(new PushbackInputStream(
                new FileInputStream(
                        new File("/home/levente/mireka/test/mireka/sieve/extended-example.sieve")),
                100));
        Program program = interpreter.compile();
        program.run(callback);
    }

    public Interpreter(PushbackInputStream in) throws IOException, SyntaxException {
        this.in = in;
        this.scanner = new Scanner();
        next = scanner.scan();
    }

    private boolean is(Kind kind) throws SyntaxException {
        return next.kind == kind;
    }

    private boolean is(EnumSet<Kind> kinds) throws SyntaxException {
        return kinds.contains(next.kind);
    }

    /**
     * Tests if the current token is a separator token with a spelling identical to the supplied
     * character.
     */
    private boolean is(char c) {
        return next instanceof SeparatorToken && ((SeparatorToken) next).value == c;
    }

    private Token takeIt() throws SyntaxException, IOException {
        Token old = next;
        next = scanner.scan();
        return old;
    }

    private void take(Kind kind) throws SyntaxException, IOException {
        if (next.kind != kind)
            throw new SyntaxException(formatSyntaxException(kind.spelling.original));
        takeIt();
    }

    private void requireExtension(Capability cap) throws SyntaxException {
        if (!extensions.contains(cap))
            throw new SyntaxException(
                    cap + " extension is not enabled, enable using the 'require' command, "
                            + "at position " + next.position);
    }

    /**
     * Takes the current token, which must be a separator token with the spelling identical to the
     * supplied character.
     */
    private void take(char c) throws SyntaxException, IOException {
        if (!is(c))
            throw new SyntaxException(formatSyntaxException(String.valueOf(c)));
        takeIt();
    }

    public Program compile() throws SyntaxException, IOException, CallbackException {
        Program program = new Program();
        program.commands = parseCommands();
        take(Eof);
        return program;
    }

    private List<Command> parseCommands() throws SyntaxException, IOException {
        List<Command> r = new ArrayList<>();
        while (is(COMMAND)) {
            r.add(parseCommand());
        }
        return r;
    }

    private Command parseCommand() throws SyntaxException, IOException {
        logger.trace("command: {}", next);
        switch (next.kind) {
        case If:
            return parseCommandIf();
        case Require:
            return parseCommandRequire();
        case Stop:
        case Discard:
            return parseCommandNullary();
        case Fileinto:
            return parseCommandFileinto();
        case Redirect:
            return parseCommandRedirect();
        case Keep:
            return parseCommandKeep();
        case Setflag:
        case Addflag:
        case Removeflag:
            return parseCommandFlag();
        default:
            assert false : next.kind;
            throw new AssertionError();
        }
    }

    private Command parseCommandIf() throws IOException, SyntaxException {
        IfControl c = new IfControl(next);
        Testcommand t;
        Block b;

        take(If);
        t = parseTest();
        b = parseBlock();
        c.add(t, b);
        while (is(Elsif)) {
            takeIt();
            t = parseTest();
            b = parseBlock();
            c.add(t, b);
        }
        if (is(Else)) {
            takeIt();
            c.elseBlock = parseBlock();
        }

        return c;
    }

    private Block parseBlock() throws SyntaxException, IOException {
        Block b = new Block();
        take('{');
        b.commands = parseCommands();
        take('}');
        return b;
    }

    private Command parseCommandRequire() throws IOException, SyntaxException {
        RequireControl c = new RequireControl(next);
        take(Require);
        c.capabilities = parseStringList();
        take(';');

        String COMPARATOR_PREFIX = "comparator-";
        for (String capability : c.capabilities) {
            if (capability.startsWith(COMPARATOR_PREFIX)) {
                String comparator = capability.substring(COMPARATOR_PREFIX.length());
                comparators
                        .add(ComparatorKind.forId(comparator).orElseThrow(() -> new SyntaxException(
                                "Required comparator is not supported: " + capability)));
            } else {
                extensions.add(Capability.forName(capability).orElseThrow(() -> new SyntaxException(
                        "Required capability is not supported: " + capability)));
            }
        }
        return c;
    }

    private Command parseCommandNullary() throws SyntaxException, IOException {
        Command r = new NullaryCommand(takeIt());
        take(';');
        return r;
    }

    private Command parseCommandFileinto() throws SyntaxException, IOException {
        requireExtension(Capability.Fileinto);
        FileintoAction c = new FileintoAction(takeIt());
        if (is(Flags)) {
            requireExtension(Capability.Imap4flags);
            takeIt();
            c.flags.strings = parseStringList();
        }
        c.mailbox = parseString();
        take(';');
        return c;
    }

    private Command parseCommandKeep() throws SyntaxException, IOException {
        KeepAction r = new KeepAction(takeIt());
        if (is(Flags)) {
            requireExtension(Capability.Imap4flags);
            takeIt();
            r.flags.strings = parseStringList();
        }
        take(';');
        return r;
    }

    private Command parseCommandRedirect() throws SyntaxException, IOException {
        take(Redirect);
        return null;

        // TODO Auto-generated method stub
    }

    private Command parseCommandFlag() throws SyntaxException, IOException {
        requireExtension(Capability.Imap4flags);
        FlagAction r = new FlagAction(takeIt());
        r.flagNames = parseStringList();
        take(';');
        return r;
    }

    private Testcommand parseTest() throws SyntaxException, IOException {
        switch (next.kind) {
        case Address:
            return parseTestAddress();
        case Allof:
        case Anyof:
            return parseTestBiLogical();
        case Envelope:
            parseTestEnvelope();
            break;
        case Exists:
            parseTestExists();
            break;
        case False:
        case True:
            return parseTestConstant();
        case Header:
            return parseTestHeader();
        case Not:
            return parseTestNot();
        case Size:
            parseTestSize();
            break;
        default:
            throw new SyntaxException(formatSyntaxException("Test command"));
        }
        return null;
    }

    private Testcommand parseTestAddress() throws SyntaxException, IOException {
        AddressTc r = new AddressTc(next);
        take(Address);
        while (is(Comparator) || is(MATCH_TYPE) || is(ADDRESS_PART)) {
            switch (next.kind) {
            case Comparator:
                parseComparator(r.comparator);
                break;
            case Is:
            case Contains:
            case Matches:
                parseMatchType(r.matchType);
                break;
            case Localpart:
            case Domain:
            case All:
                parseAddressPart(r.addressPart);
                break;
            default:
                assert false;
            }
        }
        List<String> headers = parseStringList();
        r.keys = parseStringList();

        for (String header : headers) {
            mireka.maildata.parser.Kind fieldKind = mireka.maildata.parser.Kind
                    .forHeaderFieldName(new CiString(header));
            if (!mireka.maildata.parser.Kind.ADDRESS_LISTS.contains(fieldKind))
                throw new SyntaxException(
                        "Field is not of address-list type: " + header + " at " + r.position);
            r.headers.add(fieldKind);
        }

        return r;
    }

    private Testcommand parseTestBiLogical() throws SyntaxException, IOException {
        return new BiLogicalTc(takeIt(), parseTestList());
    }

    private void parseTestEnvelope() throws SyntaxException, IOException {
        take(Envelope);
        // TODO Auto-generated method stub

    }

    private void parseTestExists() throws SyntaxException, IOException {
        take(Exists);
        // TODO Auto-generated method stub

    }

    private Testcommand parseTestConstant() throws SyntaxException, IOException {
        return new ConstantTc(takeIt());
    }

    private Testcommand parseTestHeader() throws SyntaxException, IOException {
        HeaderTc r = new HeaderTc(next);
        take(Header);
        while (is(Comparator) || is(MATCH_TYPE)) {
            switch (next.kind) {
            case Comparator:
                parseComparator(r.comparator);
                break;
            case Is:
            case Contains:
            case Matches:
                parseMatchType(r.matchType);
                break;
            default:
                assert false;
            }
        }
        r.headers = parseStringList();
        r.keys = parseStringList();
        return r;
    }

    private Testcommand parseTestNot() throws SyntaxException, IOException {
        return new NotTc(takeIt(), parseTest());
    }

    private void parseTestSize() throws SyntaxException, IOException {
        take(Size);
        // TODO Auto-generated method stub

    }

    private List<Testcommand> parseTestList() throws SyntaxException, IOException {
        List<Testcommand> r = new ArrayList<>();
        take('(');
        r.add(parseTest());
        while (is(',')) {
            takeIt();
            r.add(parseTest());
        }
        take(')');
        return r;
    }

    private void parseMatchType(MatchType state) throws SyntaxException, IOException {
        if (state.specified)
            throw new SyntaxException(
                    "match-type specifed second time at position " + next.position);
        state.value = next.kind;
        state.specified = true;
        takeIt();
    }

    private void parseAddressPart(AddressPart state) throws SyntaxException, IOException {
        if (state.specified)
            throw new SyntaxException(
                    "address-part specifed second time at position " + next.position);
        state.value = next.kind;
        state.specified = true;
        takeIt();
    }

    private void parseComparator(ComparatorType state) throws SyntaxException, IOException {
        if (state.specified)
            throw new SyntaxException(
                    "comparator specifed second time at position " + next.position);
        take(Comparator);

        String s = parseString();
        ComparatorKind kind = ComparatorKind.forId(s).orElseThrow(
                () -> new SyntaxException("Unknown comparator id at position " + next.position));
        if (!comparators.contains(kind))
            throw new SyntaxException(
                    "Comparator is not enabled in a require command at position " + next.position);
        state.value = kind;
    }

    private List<String> parseStringList() throws SyntaxException, IOException {
        List<String> v = new ArrayList<>();
        if (is('[')) {
            takeIt();
            v.add(parseString());
            while (is(',')) {
                takeIt();
                v.add(parseString());
            }
            take(']');
        } else {
            v.add(parseString());
        }
        return v;
    }

    private String parseString() throws SyntaxException, IOException {
        String v;
        if (is(QuotedString) || is(Multiline)) {
            v = ((StringToken) next).value;
            takeIt();
            return v;
        } else {
            throw new SyntaxException(formatSyntaxException("quoted-string or multi-line"));
        }
    }

    private String formatSyntaxException(String expected) {
        return "Expected " + expected + " at " + next.position + ", received '" + next + "'";
    }

    private String escapedChar(int c) {
        if (VCHAR.test(c) || c == ' ') {
            return Character.toString(c);
        } else {
            if (c == CRLF)
                return "CRLF";
            else if (c == EOF)
                return "EOF";
            else
                return String.format("%%x%02X", c);
        }
    }

    static class CharClass {
        private static int CODE_CRLF = -2;
        private static final CharClass ALPHA = new CharClass("ALPHA",
                c -> 0x41 <= c && c <= 0x5a || 0x61 <= c && c <= 0x7A);
        private static final CharClass DIGIT = new CharClass("DIGIT", c -> 0x30 <= c && c <= 0x39);
        private static final CharClass VCHAR = new CharClass("VCHAR", c -> 0x21 <= c && c <= 0x7e);
        private static final CharClass IDENTIFIER_START = new CharClass("IDENTIFIER_START",
                c -> ALPHA.test(c) || c == '_');
        private static final CharClass OCTET_NOT_CRLF = new CharClass("OCTET_NOT_CRLF",
                c -> 0x01 <= c && c <= 0x09 || 0x0B <= c && c <= 0x0C || 0x0E <= c && c <= 0xFF);
        private static final CharClass NOT_STAR = new CharClass("NOT_STAR",
                c -> c == CODE_CRLF || 0x01 <= c && c <= 0x09 || 0x0B <= c && c <= 0x0C
                        || 0x0E <= c && c <= 0x29 || 0x2B <= c && c <= 0xFF);
        private static final CharClass NOT_STAR_SLASH = new CharClass("NOT_STAR_SLASH",
                c -> c == CODE_CRLF || 0x01 <= c && c <= 0x09 || 0x0B <= c && c <= 0x0C
                        || 0x0E <= c && c <= 0x29 || 0x2B <= c && c <= 0x2E
                        || 0x30 <= c && c <= 0xFF);
        private static final CharClass OCTET_NOT_QSPECIAL = new CharClass("OCTET_NOT_QSPECIAL",
                c -> 0x01 <= c && c <= 0x09 || 0x0B <= c && c <= 0x0C || 0x0E <= c && c <= 0x21
                        || 0x23 <= c && c <= 0x5B || 0x5D <= c && c <= 0xFF);
        private static final CharClass QUOTED_SAFE = new CharClass("QUOTED_SAFE",
                c -> c == CODE_CRLF || OCTET_NOT_QSPECIAL.test(c));
        private static final CharClass QUANTIFIER = new CharClass("QUANTIFIER",
                c -> c == 'K' || c == 'M' || c == 'G' || c == 'k' || c == 'm' || c == 'g');
        private static final CharClass SEPARATOR = new CharClass("SEPARATOR",
                c -> c == '{' || c == '}' || c == '[' || c == ']' || c == '(' || c == ')'
                        || c == ',' || c == ';');

        String name;
        Predicate<Integer> test;

        public CharClass(String name, Predicate<Integer> test) {
            this.test = test;
            this.name = name;
        }

        public CharClass(char ch) {
            this.name = "\"" + ch + "\"";
            this.test = x -> x == ch;
        }

        public boolean test(int c) {
            return test.test(c);
        }
    }

    public static class Token {
        public Position position;
        public byte[] spelling;
        public Kind kind;

        public Token(Scanner scanner, Kind kind) {
            this.position = scanner.position;
            this.spelling = scanner.spelling.toByteArray();
            this.kind = kind;
        }

        @Override
        public String toString() {
            return kind.name();
        }
    }

    public static class KeywordToken extends Token {
        CiString value;

        public KeywordToken(Scanner scanner, Kind kind) {
            super(scanner, kind);
            value = new CiString(scanner.spelling());
        }
    }

    public static class NumberToken extends Token {
        long value;

        public NumberToken(Scanner scanner, Kind kind, long value) {
            super(scanner, kind);
            this.value = value;
        }
    }

    public static class StringToken extends Token {
        String value;

        public StringToken(Scanner scanner, Kind kind, String value) {
            super(scanner, kind);
            this.value = value;
        }
    }

    public static class SeparatorToken extends Token {
        int value;

        public SeparatorToken(Scanner scanner, Kind kind) {
            super(scanner, kind);
            this.value = Byte.toUnsignedInt(spelling[0]);
        }
    }

    private class Scanner {

        private CrlfScanner in = new CrlfScanner();
        /**
         * -1 EOF, -2 CRLF, otherwise the byte value read
         */
        int next;

        ByteArrayOutputStream spelling = new ByteArrayOutputStream();
        /**
         * Position of the first character of the spelling in the source.
         */
        Position position;

        public Scanner() throws IOException, SyntaxException {
            next = in.scan();
        }

        private boolean is(CharClass charclass) {
            return charclass.test.test(next);
        }

        private boolean is(char c) {
            return next == c;
        }

        public Token scan() throws SyntaxException, IOException {
            position = in.position();
            skipWhitespace();
            spelling.reset();
            position = in.position();
            if (is(IDENTIFIER_START)) {
                CiString identifier = readIdentifier();
                if (identifier.equals("text") && is(':')) {
                    return scanMultiline();
                } else {
                    return createKeywordToken();
                }
            } else if (is(':')) {
                return scanTag();
            } else if (is(DIGIT)) {
                return scanNumber();
            } else if (is('"')) {
                return scanQuotedString();
            } else if (next == EOF) {
                return new Token(this, Eof);
            } else if (is(SEPARATOR)) {
                return scanSeparatorToken();
            } else {
                throw new SyntaxException(
                        "Unexpected character " + escapedChar(next) + " at " + position);
            }
        }

        private CiString readIdentifier() throws IOException, SyntaxException {
            if (is(ALPHA) || is('_'))
                takeIt();
            else
                throw new SyntaxException(
                        formatSyntaxException("Identifier, starting with ALPHA or '_'"));
            while (is(ALPHA) || is(DIGIT) || is('_')) {
                takeIt();
            }
            return new CiString(spelling());
        }

        private Token createKeywordToken() throws SyntaxException {
            Kind kind = Kind.fromSpelling(spelling.toByteArray())
                    .orElseThrow(() -> new SyntaxException(formatSyntaxException("identifier")));
            return new KeywordToken(this, kind);
        }

        private Token scanTag() throws IOException, SyntaxException {
            takeIt();
            readIdentifier();

            Kind kind = Kind.fromSpelling(spelling.toByteArray())
                    .orElseThrow(() -> new SyntaxException(formatSyntaxException("tag")));
            return new KeywordToken(this, kind);
        }

        private Token scanNumber() throws SyntaxException, IOException {
            int c = take(DIGIT);
            long v = Character.getNumericValue(c);
            while (is(DIGIT)) {
                v = 10 * v + Character.getNumericValue(c);
            }
            if (is(QUANTIFIER)) {
                int quantifier = takeIt();
                switch (quantifier) {
                case 'k':
                case 'K':
                    v = 1024 * v;
                    break;
                case 'm':
                case 'M':
                    v = 1024 * 1024 * v;
                case 'g':
                case 'G':
                    v = 1024 * 1024 * 1024 * v;
                }
            }
            return new NumberToken(this, Number, v);
        }

        private Token scanQuotedString() throws SyntaxException, IOException {
            ByteArrayOutputStream v = new ByteArrayOutputStream();
            take('"');
            while (is(QUOTED_SAFE) || next == '\\') {
                if (is(QUOTED_SAFE)) {
                    v.write(takeIt());
                } else if (next == '\\') {
                    takeIt();
                    if (next == '"' || next == '\\') {
                        v.write(takeIt());
                    } else if (is(OCTET_NOT_QSPECIAL)) {
                        // this case should not be used according to the RFC, maybe warn
                        v.write(takeIt());
                    }
                }
            }
            take('"');

            return new StringToken(this, QuotedString, v.toString(StandardCharsets.UTF_8));
        }

        private Token scanSeparatorToken() throws IOException, SyntaxException {
            take(SEPARATOR);
            Kind kind = Kind.fromSpelling(spelling.toByteArray())
                    .orElseThrow(() -> new RuntimeException(escapedChar(next) + " " + position));
            return new SeparatorToken(this, kind);
        }

        private Token scanMultiline() {
            return null;
            // TODO Auto-generated method stub

        }

        private void skipWhitespace() throws IOException, SyntaxException {
            while (isWhitespaceChar())
                scanWhitespace();
        }

        private boolean isWhitespaceChar() throws IOException {
            return next == ' ' || next == CRLF || next == '\t' || next == '#' || isBracketComment();
        }

        private boolean isBracketComment() throws IOException {
            if (next == '/') {
                return in.peek() == '*';
            } else {
                return false;
            }
        }

        private void scanWhitespace() throws IOException, SyntaxException {
            if (next == ' ' || next == CRLF || next == '\t') {
                takeIt();
                while (next == ' ' || next == CRLF || next == '\t')
                    takeIt();
            } else {
                if (next == '#')
                    scanHashComment();
                else
                    scanBracketComment();
            }
        }

        private void scanHashComment() throws SyntaxException, IOException {
            take('#');
            while (is(OCTET_NOT_CRLF))
                takeIt();
            take(CRLF);
        }

        private void scanBracketComment() throws SyntaxException, IOException {
            take('/');
            take('*');
            while (is(NOT_STAR))
                takeIt();
            take('*');
            while (is(NOT_STAR_SLASH)) {
                takeIt();
                while (is(NOT_STAR))
                    takeIt();
                take('*');
                while (is('*'))
                    takeIt();
            }
            take('/');
        }

        private int takeIt() throws IOException, SyntaxException {
            if (next == EOF) {
                return EOF;
            } else if (next == CRLF) {
                spelling.write('\r');
                spelling.write('\n');
            } else {
                spelling.write(next);
            }
            int taken = next;
            next = in.scan();
            return taken;
        }

        private int take(int c) throws SyntaxException, IOException {
            if (next != c)
                throw new SyntaxException(formatSyntaxException(escapedChar(c)));
            return takeIt();
        }

        private int take(CharClass c) throws SyntaxException, IOException {
            if (!is(c))
                throw new SyntaxException(formatSyntaxException(c.name));
            return takeIt();
        }

        private String spelling() {
            return spelling.toString(StandardCharsets.UTF_8);
        }
    }

    private class CrlfScanner {
        /**
         * next char will start a new line
         */
        private boolean isLineCompleted = true;
        /**
         * position of the last scanned character, 1 is first row.
         */
        public int row = 0;
        /**
         * position of the last scanned character, 1 is first column.
         */
        public int column = 0;

        /**
         * Reads and sets {@link #next}. It handles the CRLF character sequence specially, as one
         * character, because the syntax is written that way.
         */
        public int scan() throws IOException, SyntaxException {
            int v;
            if (isLineCompleted) {
                row++;
                column = 0;
                isLineCompleted = false;
            }
            int next = in.read();
            if (next == '\r') {
                next = in.read();
                if (next == '\n') {
                    v = CRLF;
                    column++;
                    isLineCompleted = true;
                } else {
                    throw new SyntaxException(formatSyntaxException("LF must follow a CR"));
                }
            } else if (next == '\n') {
                column++;
                throw new SyntaxException(formatSyntaxException(
                        "CRLF line endings are required, wrong file format?"));
            } else {
                column++;
                v = next;
            }
            return v;
        }

        /**
         * Returns the next byte, but it immediately unreads it, which means that the next
         * {@link #scan()} call will return the same byte. This is useful is the current character
         * is not enough information for the lexical analyzer.
         * 
         * CRLF special handling is not implemented here, because it has not been necessary.
         */
        public int peek() throws IOException {
            int v = in.read();
            if (v != EOF)
                in.unread(v);
            return v;
        }

        public Position position() {
            return new Position(row, column);
        }

    }

}
