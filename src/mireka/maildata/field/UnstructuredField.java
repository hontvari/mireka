package mireka.maildata.field;

import java.io.IOException;

import mireka.maildata.FieldDef;
import mireka.maildata.FieldGenerator;
import mireka.maildata.HeaderField;

/**
 * @see <a href="https://tools.ietf.org/html/rfc5322#section-2.2.1">RFC 5322</a>
 *      2.2.1. Unstructured Header Field Bodies
 */
public class UnstructuredField extends HeaderField {
    /**
     * Unfolded single line, without CRLF, it may contain non-ASCII characters.
     */
    public String body;

    public UnstructuredField() {
    }

    public UnstructuredField(FieldDef<?> fieldDef, String body) {
        super(fieldDef);
        this.body = body;
    }

    @Override
    protected String generate() throws IOException {
        return new FieldGenerator().writeUnstructuredHeader(this);
    }
}
