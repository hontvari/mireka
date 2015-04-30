package mireka.maildata.parser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;

/**
 * @see <a href="https://tools.ietf.org/html/rfc2047#section-4.2">The "Q"
 *      encoding</a>
 */
public class QEncodingParser {
    private int currentChar;
    private StringReader in;
    private int position;
    private ByteArrayOutputStream out;
    byte[] result;

    public byte[] decode(String src) throws ParseException {
        in = new StringReader(src);
        out = new ByteArrayOutputStream(src.length() * 2);
        scan();

        while (currentChar != -1) {
            if (currentChar == '=') {
                scan();
                int value = 16 * scanHexDigit();
                value += scanHexDigit();
                out.write(value);
            } else if (currentChar == '_') {
                out.write(0x20);
                scan();
            } else {
                out.write(currentChar);
                scan();
            }
        }

        return out.toByteArray();
    }

    private int scanHexDigit() throws ParseException {
        int result;
        if ('0' <= currentChar && currentChar <= '9')
            result = currentChar - '0';
        else if ('A' <= currentChar && currentChar <= 'F')
            result = currentChar - 'A' + 10;
        else if ('a' <= currentChar && currentChar <= 'f')
            result = currentChar - 'a' + 10;
        else
            throw new ParseException("Hex digit expected at " + position
                    + ", received code " + currentChar + ".", position);
        scan();
        return result;
    }

    private void scan() {
        try {
            currentChar = in.read();
            position++;
        } catch (IOException e) {
            throw new RuntimeException("Assertion failed");
        }
    }

}
