package mireka.maildata.field;

import java.io.IOException;
import java.io.OutputStream;

import mireka.maildata.FieldGenerator;
import mireka.maildata.HeaderField;
import mireka.util.CharsetUtil;

/**
 * @see <a href="https://tools.ietf.org/html/rfc5322#section-2.2.1">RFC 5322</a>
 *      2.2.1. Unstructured Header Field Bodies
 */
public class UnstructuredField extends HeaderField {
    /**
     * Unfolded single line, without CRLF, it may contain non-ASCII characters.
     */
    public String body;

    @Override
    protected void writeGenerated(OutputStream out) throws IOException {
        String result = new FieldGenerator().writeUnstructuredHeader(this);
        out.write(CharsetUtil.toAsciiBytes(result));
    }
}
