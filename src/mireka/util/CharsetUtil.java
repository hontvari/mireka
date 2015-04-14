package mireka.util;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class CharsetUtil {
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
}
