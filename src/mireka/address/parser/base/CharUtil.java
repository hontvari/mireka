package mireka.address.parser.base;

public class CharUtil {

    /**
     * Converts the supplied code point to a visible character, if it is not,
     * useful for debugging.
     */
    public static String toVisibleChar(int ch) {
        if (ch == -1)
            return "EOF";
        else if (ch == 127 || ch < 32)
            return toUnicodeEscape(ch);
        else
            return "'" + (char) ch + "'";
    }

    private static String toUnicodeEscape(int ch) {
        return "\\u" + String.format("%04X", ch);
    }

}
