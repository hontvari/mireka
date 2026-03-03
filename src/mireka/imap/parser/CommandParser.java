package mireka.imap.parser;

import static mireka.imap.parser.CharClass.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Deque;
import java.util.Locale;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mireka.imap.MessageFlagSet;
import mireka.imap.SequenceSet;
import mireka.imap.Session;

/**
 * CommandParser extracts the command name from the command received from the IMAP client and
 * provides functions for parsing the arguments.
 */
public class CommandParser {
    public static final String HIERARCHY_SEPARATOR = "/";
    public static final int MAX_STRING_LITERAL = 4096;
    private Session session;
    private Scanner scanner;
    /** null if it cannot be determined because of a protocol error **/
    @Nullable
    public String tag;
    /**
     * Client command in upper case
     */
    public String command;
    /**
     * In case of the UID command there are subcommands, which data is useful for logging purposes.
     */
    public String subcommand;
    private Deque<Level> stack = new ArrayDeque<>();
    /**
     * if true the command from the client is already parsed and logged.
     */
    private boolean complete;

    public CommandParser(Session session) throws IOException {
        this.session = session;
        this.scanner = new Scanner(session.connection.input);
    }

    public void extractCommand()
            throws CommandSyntaxException, IOException, NoMoreLinesException {
        // this level represents the full command line, which is not parsed fully in this function,
        // so this level will not be closed within this function.
        beginLevel("command");
        if (scanner.isNoMoreLines)
            throw new NoMoreLinesException();
        tag = parseTag();
        take(isSpace(), "space");
        command = parseKeyword("command");
    }

    public String extractSubcommand() throws CommandSyntaxException, IOException {
        try (Level l = beginLevel("sp+subcommand")) {
            take(SPACE);
            subcommand = parseKeyword("subcommand");
            return subcommand;
        }
    }

    /**
     * Skips the remaining part of the command, it is used after it turns out that the command
     * cannot be executed successfully. It logs any not logged client lines.
     * 
     * We don't need to skip synchronized literals, because at this point, this code does not send
     * the continuation request anyway.
     */
    public void skip() throws IOException {
        if (complete)
            return;
        while (true) {
            if (next() == Scanner.EOF) {
                InputStream ris = scanner.ringBuffer.reverseInputStream();
                Literal literal = checkULiteralLineEnding(ris);
                if (literal == null) {
                    return;
                } else {
                    literal.skip();
                    scanner.start();
                }
            } else {
                take();
            }
        }
    }

    /**
     * @param ris - reverse input stream, reading from the current input position backwards
     * @return null if the stream does not end with an unsynchronizing literal
     * @throws IOException never thrown
     */
    private Literal checkULiteralLineEnding(InputStream ris) throws IOException {
        ris.reset();
        if (ris.read() != 0x0A || ris.read() != 0x0D || ris.read() != '}')
            return null;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int next = ris.read();
        while ('0' <= next && next <= '9') {
            buffer.write(next);
            next = ris.read();
        }
        if (buffer.size() == 0)
            return null;
        if (next != '{')
            return null;
        String rnumber = new String(buffer.toByteArray(), StandardCharsets.US_ASCII);
        String number = new StringBuilder(rnumber).reverse().toString();
        Literal literal = new Literal(session);
        literal.size = Long.valueOf(number);
        return literal;
    }

    public int take() throws IOException {
        return scanner.takeIt();
    }

    public int take(boolean condition, String expected) throws IOException, CommandSyntaxException {
        if (condition)
            return scanner.takeIt();
        else
            throw new CommandSyntaxException(formatCommandException(expected));
    }

    public int take(CharClass charclass) throws IOException, CommandSyntaxException {
        return take(charclass.test.test(scanner.next), charclass.name);
    }

    public int take(char ch) throws CommandSyntaxException, IOException {
        return take(next() == ch, "\"" + ch + "\"");
    }

    /**
     * @param keyword an ASCII US upper case string
     */
    public String takeKeyword(String keyword) throws CommandSyntaxException, IOException {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < keyword.length(); i++) {
            String expected = keyword.substring(i, i + 1);
            if (!isChar())
                throw new CommandSyntaxException(
                        formatCommandException(expected + " in " + keyword));
            String actualInUpperCase = Character.toString(next()).toUpperCase(Locale.US);
            if (actualInUpperCase.equals(expected))
                buf.append((char) take());
            else
                throw new CommandSyntaxException(
                        formatCommandException(expected + " in " + keyword));
        }
        return buf.toString();
    }

    /** Parses end of command */
    public void parseEof() throws CommandSyntaxException, IOException {
        take(isEof(), "EOL");
        complete = true;
    }

    public int next() {
        return scanner.next;
    }

    public Level beginLevel(String name) {
        Level l = new Level(name);
        stack.push(l);
        return l;
    }

    public Level beginPeekLevel(String name) {
        boolean startPeek;
        if (scanner.peek) {
            startPeek = false;
        } else {
            scanner.startPeek();
            startPeek = true;
        }
        Level l = new Level(name);
        l.startedPeek = startPeek;
        stack.push(l);
        return l;
    }

    public boolean is(CharClass charclass) {
        return charclass.test.test(scanner.next);
    }

    public boolean is(char c) {
        return scanner.next == c;
    }

    private String parseTag() throws IOException, CommandSyntaxException {
        try (Level l = beginLevel("tag")) {
            take(isTagChar(), "tag-char");
            while (isTagChar()) {
                take();
            }
            return l.spelling();
        }
    }

    public OffsetDateTime parseDateTime() throws CommandSyntaxException, IOException {
        try (Level l = beginLevel("date-time")) {
            take(isDquote(), "dquote");
            LocalDate date = parseDateTimeDate();
            take(isSpace(), "space");
            LocalTime time = parseTime();
            take(isSpace(), "space");
            ZoneOffset offset = parseZone();
            take(isDquote(), "dquote");
            return OffsetDateTime.of(date, time, offset);
        }
    }

    private LocalDate parseDateTimeDate() throws CommandSyntaxException, IOException {
        try (Level l = beginLevel("date-time-date")) {
            int day = parseDateDayFixed();
            take(next() == '-', "minus");
            Month month = parseDateMonth();
            take(next() == '-', "minus");
            Year year = parseDateYear();
            return LocalDate.of(year.getValue(), month, day);
        }
    }

    private LocalTime parseTime() throws CommandSyntaxException, IOException {
        try (Level l = beginLevel("time")) {
            take(isDigit(), "digit");
            take(isDigit(), "digit");
            take(next() == ':', "colon");
            take(isDigit(), "digit");
            take(isDigit(), "digit");
            take(next() == ':', "colon");
            take(isDigit(), "digit");
            take(isDigit(), "digit");
            return LocalTime.parse(l.spelling());
        }
    }

    private Year parseDateYear() throws CommandSyntaxException, IOException {
        try (Level l = beginLevel("date-year")) {
            take(isDigit(), "digit");
            take(isDigit(), "digit");
            take(isDigit(), "digit");
            take(isDigit(), "digit");
            return Year.parse(l.spelling());
        }
    }

    private Month parseDateMonth() throws CommandSyntaxException, IOException {
        try (Level l = beginLevel("date-month")) {
            take(isAlpha(), "alpha");
            take(isAlpha(), "alpha");
            take(isAlpha(), "alpha");
            switch (l.spelling().toUpperCase(Locale.US)) {
            case "JAN":
                return Month.JANUARY;
            case "FEB":
                return Month.FEBRUARY;
            case "MAR":
                return Month.MARCH;
            case "APR":
                return Month.APRIL;
            case "MAY":
                return Month.MAY;
            case "JUN":
                return Month.JUNE;
            case "JUL":
                return Month.JULY;
            case "AUG":
                return Month.AUGUST;
            case "SEP":
                return Month.SEPTEMBER;
            case "OCT":
                return Month.OCTOBER;
            case "NOV":
                return Month.NOVEMBER;
            case "DEC":
                return Month.DECEMBER;
            default:
                throw new CommandSyntaxException("Invalid month name in date-time");
            }
        }
    }

    private int parseDateDayFixed() throws IOException, CommandSyntaxException {
        try (Level l = beginLevel("date-day-fixed")) {
            StringBuilder b = new StringBuilder();
            if (isSpace()) {
                take();
                b.appendCodePoint(take(isDigit(), "digit"));
            } else {
                b.appendCodePoint(take(isDigit(), "digit"));
                b.appendCodePoint(take(isDigit(), "digit"));
            }
            return Integer.valueOf(b.toString());
        }
    }

    private ZoneOffset parseZone() throws CommandSyntaxException, IOException {
        try (Level l = beginLevel("zone")) {
            take(next() == '+' || next() == '-', "+ / -");
            take(isDigit(), "digit");
            take(isDigit(), "digit");
            take(isDigit(), "digit");
            take(isDigit(), "digit");
            return ZoneOffset.of(l.spelling());
        }
    }

    public SequenceSet parseSequenceSet() throws IOException, CommandSyntaxException {
        try (Level l = beginLevel("sequence-set")) {
            SequenceSet set = new SequenceSet();
            while (true) {
                SeqRange range = new SeqRange();
                if (next() == '$') {
                    take();
                    range.lastResult = true;
                    set.ranges.add(range);
                } else if (next() == '*' || isDigitNz()) {
                    range.begin = parseSeqNumber();
                    if (next() == ':') {
                        take();
                        range.end = parseSeqNumber();
                    } else {
                        range.end = range.begin;
                    }
                } else {
                    throw new CommandSyntaxException(formatCommandException("sequence set"));
                }
                set.ranges.add(range);
                if (next() == ',') {
                    take();
                } else {
                    return set;
                }
            }
        }
    }

    private long parseSeqNumber() throws IOException, CommandSyntaxException {
        try (Level l = beginLevel("seq-number")) {
            if (next() == '*') {
                take();
                return -1;
            } else if (isDigitNz()) {
                take();
                while (isDigit())
                    take();
                return Long.valueOf(l.spelling());
            } else {
                throw new RuntimeException();
            }
        }

    }

    /**
     * Parses case insensitive keyword like strings, like command names, for example SUBSCRIBED. It
     * always returns the string in uppercase.
     */
    public String parseKeyword(String rule) throws CommandSyntaxException, IOException {
        String name = parseAtom(rule);
        return name.toUpperCase(Locale.US);
    }

    public String parseAtom(String rule) throws CommandSyntaxException, IOException {
        try (Level l = beginLevel("atom")) {
            take(isAtomChar(), rule);
            while (isAtomChar()) {
                take();
            }
            return l.spelling();
        }
    }

    public String parseAstring(String rule) throws IOException, CommandSyntaxException {
        try (Level l = beginLevel("astring")) {
            if (isAstringChar()) {
                while (isAstringChar())
                    take();
                return l.spelling();
            } else {
                return parseString();
            }
        }
    }

    public String parseString() throws CommandSyntaxException, IOException {
        try (Level l = beginLevel("string")) {
            if (isDquote())
                return parseQuoted();
            else
                return parseLiteralString();
        }
    }

    private String parseQuoted() throws CommandSyntaxException, IOException {
        try (Level l = beginLevel("quoted")) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            take(isDquote(), "dquote");
            do {
                if (isTextChar() && !isQuotedSpecials()) {
                    buffer.write(take());
                } else if (next() == '\\') {
                    take();
                    buffer.write(take(isQuotedSpecials(), "quoted-specials"));
                } else if (isUtf82Start()) {
                    buffer.write(take());
                    buffer.write(take(isUtf8tail(), "utf8-2"));
                } else if (isUtf83Start()) {
                    buffer.write(take());
                    buffer.write(take(isUtf8tail(), "utf8-3"));
                    buffer.write(take(isUtf8tail(), "utf8-3"));
                } else if (isUtf84Start()) {
                    buffer.write(take());
                    buffer.write(take(isUtf8tail(), "utf8-4"));
                    buffer.write(take(isUtf8tail(), "utf8-4"));
                    buffer.write(take(isUtf8tail(), "utf8-4"));
                } else {
                    break;
                }
            } while (true);
            String result = buffer.toString(StandardCharsets.UTF_8);
            take(isDquote(), "dquote");
            return result;
        }
    }

    private String parseLiteralString() throws CommandSyntaxException, IOException {
        try (Level l = beginLevel("literal-string")) {
            Literal literal = parseLiteralHeader();
            if (literal.size > MAX_STRING_LITERAL) {
                literal.skip();
                if (!literal.synchronizing)
                    scanner.start();
                throw new CommandSyntaxException("Literal string is too long: " + literal.size
                        + ", maximum allowed: " + MAX_STRING_LITERAL);
            }
            InputStream lin = literal.getLimitedInputStream();
            String result = new String(lin.readAllBytes(), StandardCharsets.UTF_8);
            scanner.start();
            return result;
        }
    }

    /**
     * After calling this, either
     * <ul>
     * <li>the literal bytes must be read and after that the scanner must be restarted.
     * <li>the not synchronized literal bytes must be skipped and than the scanner restarted
     * <li>the synchronized literal bytes must be refused by not sending a continuation request and
     * the scanner must not be restarted
     * </ul>
     */
    public Literal parseLiteralHeader() throws CommandSyntaxException, IOException {
        try (Level l = beginLevel("literal-header")) {
            Literal literal = new Literal(session);
            take(next() == '{', "opening-brace");
            literal.size = parseNumber64();
            if (next() == '+') {
                literal.synchronizing = false;
                take();
            } else {
                literal.synchronizing = true;
            }
            take(next() == '}', "closing-brace");
            take(isEof(), "EOL");
            return literal;
        }
    }

    public long parseNumber64() throws IOException, CommandSyntaxException {
        try (Level l = beginLevel("number64")) {
            take(isDigit(), "digit");
            while (isDigit()) {
                take();
            }
            return Long.parseUnsignedLong(l.spelling());
        }
    }

    public long parseNzNumber() throws CommandSyntaxException, IOException {
        try (Level l = beginLevel("nz-number")) {
            take(isDigitNz(), "digit-nz");
            while (isDigit()) {
                take();
            }
            return Long.parseUnsignedLong(l.spelling());
        }
    }

    public byte[] parseBase64Opt(String rule) throws IOException, CommandSyntaxException {
        if (next() == '=') {
            take();
            return new byte[0];
        } else {
            return parseBase64(rule);
        }
    }

    private byte[] parseBase64(String rule) throws CommandSyntaxException, IOException {
        try (Level l = beginLevel(rule + "-base64")) {
            take(isBase64Char(), "base64-char");
            take(isBase64Char(), "base64-char");
            int char3 = take(isBase64Char() || next() == '=', "base64-char-or-equal");
            if (char3 == '=') {
                take(next() == '=', "equal");
            } else {
                take(isBase64Char() || next() == '=', "base64-char-or-equal");
            }
            return Base64.getDecoder().decode(l.spelling());
        }
    }

    /**
     * Parses a parenthesized message flag list, which may be empty.
     */
    public MessageFlagSet parseFlagList() throws IOException, CommandSyntaxException {
        try (Level l = beginLevel("flag-list")) {
            MessageFlagSet flags = new MessageFlagSet();
            take('(');
            if (is('\\') || is(ATOM_CHAR)) {
                flags.add(parseFlag());
                while (is(' ')) {
                    take();
                    flags.add(parseFlag());
                }
            }
            take(')');
            return flags;
        }
    }

    public String parseFlag() throws IOException, CommandSyntaxException {
        try (Level l = beginLevel("flag")) {
            if (is('\\')) {
                take();
                String atomPart = parseKeyword("flag-atom");
                switch (atomPart) {
                case "ANSWERED":
                case "FLAGGED":
                case "DELETED":
                case "SEEN":
                case "DRAFT":
                    break;
                default:
                    throw new CommandSyntaxException("Unknown system flag: " + l.spelling());
                }
            } else if (is(ATOM_CHAR)) {
                parseAtom("flag-keyword");
            } else {
                throw new CommandSyntaxException(formatCommandException("flag"));
            }
            return l.spelling();
        }
    }

    public String peekKeyword() throws IOException {
        return scanner.peekAtom().toUpperCase(Locale.US);
    }

    public String formatCommandException(String expected) {
        return "Expected " + expected + " at " + scanner.position + ", received '" + escapedChar()
                + "'";
    }

    public String formatKeywordException(String expected, Level level) {
        return "Expected " + expected + " at " + level.begin + ", received '" + level.spellingHead()
                + "'";
    }

    private String escapedChar() {
        if (isVchar() || next() == ' ') {
            return Character.toString(next());
        } else {
            return String.format("%%x%02X", next());
        }
    }

    private void appendToSpelling(int next) {
        for (Level l : stack) {
            l.appendToSpelling(next);
        }
    }

    private boolean isEof() {
        return next() == -1;
    }

    private boolean isBase64Char() {
        return isAlpha() || isDigit() || next() == '+' || next() == '/';
    }

    public boolean isDigit() {
        return 0x30 <= next() && next() <= 0x39;
    }

    public boolean isDigitNz() {
        return 0x31 <= next() && next() <= 0x39;
    }

    private boolean isUtf82Start() {
        return 0xC2 <= next() && next() <= 0xDF;
    }

    private boolean isUtf83Start() {
        return 0xE0 <= next() && next() <= 0xEF;
    }

    private boolean isUtf84Start() {
        return 0xF0 <= next() && next() <= 0xF4;
    }

    private boolean isUtf8tail() {
        return 0x80 <= next() && next() <= 0xBF;
    }

    private boolean isTextChar() {
        return isChar() && !isCr() && !isLf();
    }

    private boolean isCr() {
        return next() == 0x0D;
    }

    private boolean isLf() {
        return next() == 0x0A;
    }

    public boolean isAlpha() {
        return 'A' <= next() && next() <= 'Z' || 'a' <= next() && next() <= 'z';
    }

    /** is visible (printing) character **/
    private boolean isVchar() {
        return 0x21 <= next() && next() <= 0x7E;
    }

    private boolean isTagChar() {
        return isAstringChar() && next() != '+';
    }

    private boolean isAstringChar() {
        return isAtomChar() || isRespSpecials();
    }

    public boolean isAtomChar() {
        return isChar() && !isAtomSpecials();
    }

    /** any 7-bit US-ASCII character excluding NUL **/
    private boolean isChar() {
        return 1 <= next() && next() <= 0x7F;
    }

    private boolean isAtomSpecials() {
        return next() == '(' || next() == ')' || next() == '{' || isSpace() || isCtl()
                || isListWildcards() || isQuotedSpecials() || isRespSpecials();
    }

    public boolean isSpace() {
        return next() == ' ';
    }

    private boolean isCtl() {
        return 0 <= next() && next() <= 0x1F || next() == 0x7F;
    }

    private boolean isListWildcards() {
        return next() == '%' || next() == '*';
    }

    private boolean isQuotedSpecials() {
        return isDquote() || next() == '\\';
    }

    public boolean isDquote() {
        return next() == '"';
    }

    private boolean isRespSpecials() {
        return next() == ']';
    }

    public boolean isListChar() {
        return isAtomChar() || isListWildcard() || isRespSpecials();
    }

    private boolean isListWildcard() {
        return next() == '%' || next() == '*';
    }

    public class Level implements AutoCloseable {
        private final Logger logger = LoggerFactory.getLogger(CommandParser.Level.class);
        static final int MAX_SPELLING = 1024;
        String name;
        ByteArrayOutputStream spelling = new ByteArrayOutputStream(128);
        boolean spellingOverflow;
        /**
         * true if the level should receive spelling in peek-mode. It means the level has been
         * started while peek mode was already active.
         */
        boolean peek;
        /**
         * true if close should stop the scanner's peek mode
         */
        public boolean startedPeek;
        int begin;

        Level(String name) {
            this.name = name;
            this.peek = scanner.peek;
            this.begin = scanner.position;
            logger.trace("beginLevel {}{}", peek ? "peek " : "", name);
        }

        private void appendToSpelling(int next) {
            if (scanner.peek && !peek)
                return;
            if (next != -1) {
                if (spelling.size() < MAX_SPELLING)
                    spelling.write(next);
                else
                    spellingOverflow = true;
            }
        }

        public String spelling() throws CommandSyntaxException {
            if (spellingOverflow)
                throw new CommandSyntaxException("Element too long: " + name);
            return spelling.toString(StandardCharsets.UTF_8);
        }

        public String spellingHead() {
            String overflowSign;
            if (spellingOverflow)
                overflowSign = "...";
            else
                overflowSign = "";
            return spelling.toString(StandardCharsets.UTF_8) + overflowSign;
        }

        @Override
        public void close() {
            if (logger.isTraceEnabled())
                logger.trace("endLevel   {}{} {}",
                        new Object[] { peek ? "peek " : "", name, spellingHead() });
            if (startedPeek)
                scanner.stopPeek();
            stack.pop();
        }
    }

    /**
     * In addition to being a scanner for parsing it is also a line reader. Its specialty is that it
     * returns EOF on CRLF, and it only continue reading the stream on explicit instruction. If it
     * reach CRLF, it reads it, and from that point it returns -1 EOF token. It does not return the
     * CRLF byte pair. It starts to read again a new line after {@link #start} is called.
     */
    private class Scanner {
        private static final int MAX_PEEK = 128;
        private final Logger logger = LoggerFactory.getLogger(CommandParser.Scanner.class);
        private static final int CR = 0x0D, LF = 0x0A, EOF = -1;

        PushbackInputStream in;
        /**
         * the next byte or -1 on EOL. (Not on EOF! An actual EOF before a CRLF causes an exception)
         */
        int next = -1;
        /**
         * The index of {@link #next} in {@link #in}, within the current line.
         */
        int position = 0;
        /**
         * true means it reached and read EOL, the scanner returns EOF from now, until start()
         * explicitly called.
         */
        boolean stopped = false;
        /**
         * true if the scanner is in peek mode. At the end of the peek mode, bytes read will be
         * pushed back into the input stream.
         */
        boolean peek;
        ByteArrayOutputStream peekBuffer = new ByteArrayOutputStream();
        /**
         * state before peek
         */
        State oldState = new State();
        /**
         * not updated in peek mode
         */
        RingBuffer ringBuffer = new RingBuffer();
        /**
         * not updated in peek mode
         */
        LineHead maskedLine = new LineHead();

        /**
         * It is true at the start of the command before any character is parsed. If true, an
         * immediate EOF does not result in an exception, but in setting of the isNoMoreLines
         * variable.
         */
        boolean isCommandStart;
        /**
         * True, if at the beginning of a new command - indicated by {@link #isCommandStart}, the
         * input stream is closed, and an EOF is read.
         */
        public boolean isNoMoreLines;

        /**
         * This constructor must be called at the beginning of a command, because it handles an EOF
         * differently at the start of a command line than at any later point. Specifically it sets
         * {@link #isNoMoreLines} instead of throwing an exception.
         */
        Scanner(PushbackInputStream in) throws IOException {
            this.in = in;
            isCommandStart = true;
            readNext();
        }

        private void readNext() throws IOException {
            next = in.read();
            appendToPeekBuffer(next);
            if (next == CR) {
                next = in.read();
                appendToPeekBuffer(next);
                if (next == LF) {
                    stopped = true;
                    next = EOF;
                } else {
                    throw new LineFormatException("LF expected after CR, but received " + next
                            + " at " + position + " line: " + maskedLine.toString());
                }
                if (!peek)
                    session.protocolLogger.logClient(maskedLine);
            } else if (next == LF) {
                session.protocolLogger.logClient(maskedLine);
                throw new LineFormatException(
                        "Unexpected LF at " + position + " line: " + maskedLine.toString());
            } else if (next == EOF) {
                if (isCommandStart) {
                    isNoMoreLines = true;
                } else {
                    session.protocolLogger.logClient(maskedLine);
                    throw new LineFormatException(
                            "EOF before CRLF at " + position + " line: " + maskedLine.toString());
                }
            }
        }

        int takeIt() throws IOException {
            if (logger.isTraceEnabled())
                logger.trace(String.format("scanner.takeIt %2X %s", next,
                        next == EOF ? "EOF" : String.valueOf((char) next)));
            if (stopped)
                return EOF;
            // it is never EOF here
            int takenChar = next;
            appendToSpelling(takenChar);
            if (!peek) {
                maskedLine.append(takenChar);
                ringBuffer.add((byte) takenChar);
            }
            readNext();
            position++;
            return takenChar;
        }

        void start() throws IOException {
            if (!stopped)
                throw new IllegalStateException();
            position = 0;
            stopped = false;
            maskedLine.reset();
            ringBuffer.reset();
            readNext();
        }

        void startPeek() {
            if (stopped)
                throw new IllegalStateException();
            if (peek)
                throw new IllegalStateException();
            peek = true;
            oldState.next = next;
            oldState.position = position;
        }

        void stopPeek() {
            peek = false;
            next = oldState.next;
            position = oldState.position;
            byte[] peekBytes = peekBuffer.toByteArray();
            try {
                in.unread(peekBytes);
            } catch (IOException e) {
                throw new RuntimeException("peekBuffer is too long: " + peekBytes.length);
            }
            peekBuffer.reset();
        }

        private void appendToPeekBuffer(int b) throws PeekException {
            if (peek && b != EOF) {
                if (peekBuffer.size() >= MAX_PEEK)
                    throw new PeekException("peek too long");
                peekBuffer.write(b);
            }
        }

        public String peekAtom() throws IOException {
            int oldnext = next;
            StringBuilder sb = new StringBuilder();
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            if (!isAtomChar())
                throw new IllegalStateException("atom is expected");
            sb.append((char) next);
            next = in.read();
            if (next != EOF)
                b.write(next);
            while (isAtomChar()) {
                sb.append(next);
                if (b.size() >= MAX_PEEK)
                    break;
                next = in.read();
                if (next != EOF)
                    b.write(next);
            }
            in.unread(b.toByteArray());
            next = oldnext;
            return sb.toString();
        }

        private boolean isAtomChar() {
            return isChar() && !isAtomSpecials();
        }

        /** any 7-bit US-ASCII character excluding NUL **/
        private boolean isChar() {
            return 1 <= next && next <= 0x7F;
        }

        private boolean isAtomSpecials() {
            return next == '(' || next == ')' || next == '{' || isSpace() || isCtl()
                    || isListWildcards() || isQuotedSpecials() || isRespSpecials();
        }

        private boolean isSpace() {
            return next == ' ';
        }

        private boolean isCtl() {
            return 0 <= next && next <= 0x1F || next == 0x7F;
        }

        private boolean isListWildcards() {
            return next == '%' || next == '*';
        }

        private boolean isQuotedSpecials() {
            return isDquote() || next == '\\';
        }

        public boolean isDquote() {
            return next == '"';
        }

        private boolean isRespSpecials() {
            return next == ']';
        }

        private class State {
            int next;
            int position;
        }
    }

    private class RingBuffer {
        byte[] bytes = new byte[16];
        /** exclusive **/
        int tail = 0;
        /** inclusive **/
        int size = 0;

        void add(byte b) {
            bytes[tail++] = b;
            if (tail >= bytes.length)
                tail = 0;
            if (size <= bytes.length)
                size++;
        }

        public void reset() {
            tail = 0;
            size = 0;
        }

        InputStream reverseInputStream() {
            return new InputStream() {
                int index = 0;

                @Override
                public int read() throws IOException {
                    if (index >= size)
                        return -1;
                    int arraypos = tail - 1 - index;
                    if (arraypos < 0)
                        arraypos = arraypos + bytes.length;
                    index++;
                    return Byte.toUnsignedInt(bytes[arraypos]);
                }

                @Override
                public void reset() throws IOException {
                    index = 0;
                }
            };
        }
    }

    /**
     * Signals a scanner peek, which is longer than allowed.
     */
    private class PeekException extends IOException {
        private static final long serialVersionUID = 996289996859983356L;

        public PeekException(String message) {
            super(message);
        }
    }
}
