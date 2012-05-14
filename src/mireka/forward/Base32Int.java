package mireka.forward;

import java.util.Locale;

/**
 * Base32Int class can encode and decode an int value using a Base32.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc4648">RFC 4648</a>
 */
class Base32Int {
    private static final String BASE32_TABLE =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    public static int decode(String text) throws NumberFormatException {
        String timestampUpperCase = text.toUpperCase(Locale.US);
        int value = 0;
        for (int i = 0; i < timestampUpperCase.length(); i++) {
            char ch = timestampUpperCase.charAt(i);
            int digitValue = BASE32_TABLE.indexOf(ch);
            if (digitValue == -1)
                throw new NumberFormatException("Invalid Base32 digit in "
                        + text);
            value = (value << 5) + digitValue;
        }
        return value;
    }

    public static String encode10Bits(int value) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(BASE32_TABLE.charAt(value >> 5 & 0x1F));
        buffer.append(BASE32_TABLE.charAt(value & 0x1F));
        return buffer.toString();
    }

}
