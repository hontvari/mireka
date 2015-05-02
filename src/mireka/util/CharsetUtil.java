package mireka.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;

public class CharsetUtil {
    public static final Charset ASCII = Charset.forName("US-ASCII");

    public static String toAsciiLowerCase(String s) {
        return s.toLowerCase(Locale.US);
    }

    public static byte[] toAsciiBytes(String s) {
        try {
            return s.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    /**
     * Converts the supplied ASCII encoded byte array to a String, replacing
     * illegal byte values with a replacement character.
     */
    public static String toAsciiCharacters(byte[] byteString) {
        return new String(byteString, ASCII);
    }
}
