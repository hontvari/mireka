package mireka.imap.parser;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Collect the characters of a line, but after about MAX_STORED bytes, it swallows further
 * characters. For example it is useful for logging lines sent by the server, or received from the
 * client.
 */
public class LineHead {
    public static final int MAX_STORED = 1024;
    private static final int CR = 0x0D, LF = 0x0A;
    private static final byte[] CRLF = new byte[] { CR, LF };

    public ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    public boolean overflow;
    public int length;

    public void append(int b) {
        if (buffer.size() < MAX_STORED) {
            buffer.write(b);
            length++;
        } else if (buffer.size() == MAX_STORED) {
            overflow = true;
        }
    }

    public void append(byte ch) {
        append(Byte.toUnsignedInt(ch));
    }

    public void append(byte[] bytes) {
        if (buffer.size() + bytes.length <= MAX_STORED) {
            buffer.writeBytes(bytes);
            length += bytes.length;
        } else {
            for (byte b : bytes) {
                append(b);
            }
        }
    }

    String toString(Charset charset) {
        byte[] bytes = buffer.toByteArray();
        if (arrayEndsWith(bytes, CRLF))
            bytes = Arrays.copyOf(bytes, bytes.length - 2);
        else if (arrayEndsWith(bytes, (byte) CR))
            bytes = Arrays.copyOf(bytes, bytes.length - 1);
        int utf8tails = 0;
        while (bytes.length > utf8tails && isUtf8tail(bytes[bytes.length - 1 - utf8tails]))
            utf8tails++;
        if (utf8tails > 0)
            bytes = Arrays.copyOf(bytes, bytes.length - utf8tails);

        String result = new String(bytes, StandardCharsets.UTF_8);
        if (overflow)
            result += "...";
        return result;
    }

    private static boolean isUtf8tail(int b) {
        return 0x80 <= b && b <= 0xBF;
    }

    private static boolean arrayEndsWith(byte[] a, byte[] end) {
        if (a.length < end.length)
            return false;
        for (int i = 0; i < end.length; i++) {
            if (end[i] != a[a.length - end.length + i])
                return false;
        }
        return true;
    }

    private static boolean arrayEndsWith(byte[] a, byte end) {
        return a.length >= 1 && a[a.length - 1] == end;
    }

    @Override
    public String toString() {
        return toString(StandardCharsets.UTF_8);
    }

    void reset() {
        buffer.reset();
        overflow = false;
    }
}