package mireka.maildata;

import java.io.IOException;
import java.io.OutputStream;

import mireka.util.CharsetUtil;

/**
 * @see <a href="https://tools.ietf.org/html/rfc5322#section-2.2.1">RFC 5322</a>
 *      2.2.1. Unstructured Header Field Bodies
 */
public class UnstructuredHeader extends HeaderField {
    /**
     * unfolded single line, without CRLF
     */
    public String body;

    @Override
    protected void writeGenerated(OutputStream out) throws IOException {
        String result = new FieldWriter().writeUnstructuredHeader(this);
        out.write(CharsetUtil.toAsciiBytes(result));
    }
}
