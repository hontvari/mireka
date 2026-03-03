package mireka.imap.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import mireka.imap.CopyuidResponseCode;
import mireka.imap.MailboxFlag;
import mireka.imap.Namespace;
import mireka.imap.NamespaceCatalogue;
import mireka.imap.ParenthesizedList;
import mireka.imap.ResponseCode;
import mireka.imap.SequenceSet;
import mireka.imap.server.ConcurrentOutputStream.Substream;
import mireka.imap.server.ProtocolLogger;

public class Generator {
    private static final int MAX_QUOTED = 64;
    private static final byte[] CRLF = new byte[] { '\r', '\n' };
    private static final int LF = '\n';
    public final Substream out;
    private final ProtocolLogger protocolLogger;
    private int next;
    private byte[] in;
    private int pos;
    private LineHead line = new LineHead();

    public Generator(Substream out, ProtocolLogger protocolLogger) {
        this.out = out;
        this.protocolLogger = protocolLogger;
    }

    /** In addition to writing out a CRLF, it also logs the line */
    public void writeCRLF() throws IOException {
        out.write(CRLF);
        protocolLogger.logServer(line);
        line.reset();
    }

    /** Calls flush on the underlying output stream, unlocks the socket output stream */
    public void flush() throws IOException {
        out.flush();
    }

    /**
     * Writes out a CRLF, logs the line, flushes the stream and unlocks the socket output stream.
     */
    public void flushLine() throws IOException {
        writeCRLF();
        flush();
    }

    public void write(String s) throws IOException {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        line.append(bytes);
        out.write(bytes);
    }

    /**
     * Surrogate pairs are not allowed. This is mostly useful for ASCII characters.
     */
    public void write(char c) throws IOException {
        if (c <= 0x7F) {
            line.append(c);
            out.write(c);
        } else {
            byte[] bytes = Character.toString(c).getBytes(StandardCharsets.UTF_8);
            line.append(bytes);
            out.write(bytes);
        }
    }

    /**
     * The supplied byte is written out as it is, for example it can be the second byte of an UTF-8
     * encoded character. Only values between 0x00 and 0xFF are allowed.
     */
    public void write(int b) throws IOException {
        if (b < 0 || b > 0xFF)
            throw new IllegalArgumentException(Integer.toHexString(b));
        line.append(b);
        out.write(b);
    }

    /**
     * The supplied byte is written out as it is, for example it can be the second byte of an UTF-8
     * encoded character.
     */
    public void write(byte b) throws IOException {
        line.append(b);
        out.write(b);
    }

    public void writeCodepoint(int codepoint) throws IOException {
        if (codepoint < 0)
            throw new IllegalArgumentException();
        if (codepoint <= 0x7F) {
            line.append(codepoint);
            out.write(codepoint);
        } else {
            byte[] bytes = Character.toString(codepoint).getBytes(StandardCharsets.UTF_8);
            line.append(bytes);
            out.write(bytes);
        }
    }

    public void writeAtom(String atom) throws IOException {
        set(atom);
        if (!isAtomChar())
            throw new RuntimeException("Atom must start with an ATOM-CHAR: " + atom);
        while (isAtomChar()) {
            take();
        }
        if (!isEof())
            throw new RuntimeException("Atom must consists of ATOM-CHAR elements: " + atom);
        write(atom);
    }

    public void writeAstring(String src) throws IOException {
        if (src.length() >= 1 && isAstringChars(src))
            write(src);
        else {
            set(src);
            writeQuoted();
        }
    }

    private void writeQuoted() throws IOException {
        write('"');
        while (next != -1) {
            if (isTextChar() && !isQuotedSpecials()) {
                write(next);
            } else if (isQuotedSpecials()) {
                write('\\');
                write(next);
            } else if (isUtf82() || isUtf83() || isUtf84()) {
                write(next);
            } else {
                throw new RuntimeException("Unexpected character: " + next);
            }
            take();
        }
        write('"');
    }

    public void writeString(String v) throws IOException {
        byte[] bytes = v.getBytes(StandardCharsets.UTF_8);
        InputStream data = new ByteArrayInputStream(bytes);
        writeString(data, bytes.length);
    }

    public void writeString(InputStream data, long length) throws IOException {
        if (length <= MAX_QUOTED) {
            byte[] bytes = data.readNBytes((int) length);
            if (bytes.length != length)
                throw new RuntimeException(String.valueOf(in.length) + "/" + length);
            if (isQuotableBytes(bytes)) {
                set(bytes);
                writeQuoted();
            } else {
                writeLiteral(new ByteArrayInputStream(bytes), length);
            }
        } else {
            writeLiteral(data, length);
        }
    }

    /**
     * If the supplied data stream is null, than "NIL" is written out.
     */
    public void writeNstring(@Nullable InputStream data, long length) throws IOException {
        if (data == null) {
            writeNil();
            return;
        } else {
            writeString(data, length);
        }
    }

    public void writeNstring(@Nullable String v) throws IOException {
        if (v == null) {
            writeNil();
        } else {
            writeString(v);
        }
    }

    private boolean isQuotableBytes(byte[] bytes) {
        set(bytes);
        while (!isEof()) {
            if (isCr() || isLf() || isNull())
                return false;
            take();
        }
        return true;
    }

    private void writeLiteral(InputStream data, long length) throws IOException {
        write('{');
        write(Long.toString(length));
        write('}');
        writeCRLF();
        transferInputStream(data, out);
    }

    private void transferInputStream(InputStream in, OutputStream out) throws IOException {
        int i = 0;
        int b;
        LineHead line = new LineHead();
        while ((b = in.read()) != -1) {
            protocolLogger.logServerLiteralByte(i, b);
            line.append(b);
            out.write(b);
            if (b == LF) {
                protocolLogger.logServerLiteral(line);
                line.reset();
            }
        }
    }

    /**
     * Non-zero unsigned 32-bit integer
     */
    public void writeNzNumber(long n) throws IOException {
        write(Long.toUnsignedString(n));
    }

    private void writeNil() throws IOException {
        write("NIL");
    }

    public void writeResponseCodeCopy(CopyuidResponseCode v) throws IOException {
        write("COPYUID ");
        writeNzNumber(v.uidvalidity);
        write(' ');
        writeUidSet(v.srcSeq);
        write(' ');
        writeUidSet(v.dstSeq);
    }

    public void writeResponseCodeCapability(List<String> capabilities) throws IOException {
        write("CAPABILITY");
        for (String s : capabilities) {
            write(' ');
            write(s);
        }
    }

    private void writeUidSet(SequenceSet seq) throws IOException {
        boolean first = true;
        for (SeqRange range : seq.ranges) {
            if (first)
                first = false;
            else
                write(',');
            if (range.isNumber()) {
                writeNzNumber(range.begin);
            } else {
                writeNzNumber(range.begin);
                write(':');
                writeNzNumber(range.end);
            }
        }
    }

    /**
     * Send a generic status response.
     * 
     * @param tag null means untagged, same as "*".
     * @param name name of the specific response type: "OK", "NO", "BAD", "PREAUTH", "BYE"
     * @param humanReadableText it must be at least one character, cannot contain CR and LF.
     */
    public void respondGenericStatus(@Nullable String tag, String name, @Nullable ResponseCode code,
            String humanReadableText) throws IOException {
        write(tag == null ? "*" : tag);
        write(' ');
        write(name);
        write(' ');
        if (code != null) {
            write('[');
            code.generate(this);
            write("] ");
        }
        if (!(humanReadableText.length() >= 1))
            throw new IllegalArgumentException();
        write(humanReadableText);
        flushLine();
    }

    public void respondGreetingOk(ResponseCode capabilities, String server) throws IOException {
        respondUntaggedOk(capabilities, server + " Mireka IMAP4rev2 server ready");
    }

    public void respondGreetingBye(String server, String reason) throws IOException {
        respondUntaggedOk(null, server + " Mireka IMAP4rev2 server; " + reason);
    }

    public void respondUntaggedOk(@Nullable ResponseCode code, String humanReadableText)
            throws IOException {
        respondGenericStatus("*", "OK", code, humanReadableText);
    }

    public void respondBye(@Nullable ResponseCode code, String humanReadableText)
            throws IOException {
        respondGenericStatus("*", "BYE", code, humanReadableText);
    }

    public void respondBye(String humanReadableText) throws IOException {
        respondGenericStatus("*", "BYE", null, humanReadableText);
    }

    public void respondEnabled(List<String> enabledCaps) throws IOException {
        write("* ENABLED");
        for (String cap : enabledCaps) {
            write(' ');
            writeAtom(cap);
        }
        flushLine();
    }

    public void respondCapability(List<String> capabilities) throws IOException {
        write("* CAPABILITY");
        for (String s : capabilities) {
            write(' ');
            write(s);
        }
        flushLine();
    }

    public void respondList(EnumSet<MailboxFlag> attributes, String name,
            ParenthesizedList extension) throws IOException {
        write("* LIST");
        write(" (");
        write(attributes.stream().map(f -> f.imapName).collect(Collectors.joining(" ")));
        write(')');
        write(" \"/\"");
        write(' ');
        writeAstring(name);
        if (!extension.elements.isEmpty()) {
            write(' ');
            extension.generate(this);
        }
        flushLine();
    }

    public void respondLsub(EnumSet<MailboxFlag> attributes, String name,
            ParenthesizedList extension) throws IOException {
        write("* LSUB");
        write(" (");
        write(attributes.stream().map(f -> f.imapName).collect(Collectors.joining(" ")));
        write(')');
        write(" \"/\"");
        write(' ');
        writeAstring(name);
        if (!extension.elements.isEmpty()) {
            write(' ');
            extension.generate(this);
        }
        flushLine();
    }

    public void respondNamespace(NamespaceCatalogue namespaces) throws IOException {
        write("* NAMESPACE ");
        writeNamespace(namespaces.personals);
        write(' ');
        writeNamespace(namespaces.others);
        write(' ');
        writeNamespace(namespaces.shareds);
        flushLine();
    }

    private void writeNamespace(List<Namespace> namespaces) throws IOException {
        if (namespaces.isEmpty()) {
            writeNil();
        } else {
            write('(');
            for (Namespace ns : namespaces) {
                write('(');
                writeString(ns.prefix);
                write(' ');
                writeNstring(ns.delimiter);
                write(')');
            }
            write(')');
        }
    }

    public void respondFlags(List<String> flags) throws IOException {
        write("* FLAGS (");
        int index = 0;
        for (String f : flags) {
            if (index++ != 0)
                write(' ');
            write(f);
        }
        write(')');
        flushLine();
    }

    private void respondUntagged(CharSequence s) throws IOException {
        write("* ");
        write(s.toString());
        flushLine();
    }

    public void respondExists(long count) throws IOException {
        respondUntagged(count + " EXISTS");
    }

    public void respondExpunge(long seq) throws IOException {
        write("* ");
        writeNzNumber(seq);
        write(" EXPUNGE");
        flushLine();
    }

    public void respondFetch(long seq, ParenthesizedList data) throws IOException {
        write("* ");
        write(Long.toUnsignedString(seq));
        write(" FETCH ");
        data.generate(this);
        flushLine();
    }

    /**
     * It is sent when the server wants to indicate that it request additional data (in case of the
     * AUTHENTICATE command) or after a synchronizing literal.
     * 
     * @param text human readable text or in case of the AUTHENTICATE command a server challenge.
     */
    public void respondCommandContinuationRequest(@Nullable String text)
            throws IOException {
        write("+ ");
        if (text != null)
            write(text);
        flushLine();
    }

    private boolean isUtf82() {
        return 0x80 <= next && next <= 0x7FF;
    }

    private boolean isUtf83() {
        return 0x800 <= next && next <= 0xFFFF;
    }

    private boolean isUtf84() {
        return 0x10000 <= next && next <= 0x10FFFF;
    }

    private boolean isAstringChars(String src) {
        set(src);
        while (next != -1) {
            if (!isAstringChar())
                return false;
            take();
        }
        return true;
    }

    private boolean isAstringChar() {
        return isAtomChar() || isRespSpecials();
    }

    private boolean isAtomChar() {
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

    private boolean isDquote() {
        return next() == '"';
    }

    private boolean isRespSpecials() {
        return next() == ']';
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

    private boolean isNull() {
        return next() == 0;
    }

    private boolean isEof() {
        return next() == -1;
    }

    private int next() {
        return next;
    }

    private int take() {
        int old = next;
        if (pos < in.length) {
            pos++;
            if (pos < in.length) {
                next = Byte.toUnsignedInt(in[pos]);
            } else {
                next = -1;
            }
        }
        return old;
    }

    private void set(String src) {
        in = src.getBytes(StandardCharsets.UTF_8);
        pos = 0;
        next = in.length >= 1 ? Byte.toUnsignedInt(in[0]) : -1;
    }

    private void set(byte[] src) {
        in = src;
        pos = 0;
        next = in.length >= 1 ? Byte.toUnsignedInt(in[0]) : -1;
    }

}
