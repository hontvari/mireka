package mireka.maildata.field;

import java.io.IOException;

import mireka.maildata.HeaderField;
import mireka.maildata.parser.FieldGenerator;
import mireka.maildata.parser.Kind;

/**
 * @see <a href="https://tools.ietf.org/html/rfc5322#section-2.2.1">RFC 5322</a>
 *      2.2.1. Unstructured Header Field Bodies
 */
public class UnstructuredField extends HeaderField {
    /**
     * This constuctor should be used when parsing an unstructured field of a mail.
     */
    public UnstructuredField(Kind kind) {
        super(kind);
    }

    /**
     * This constructor can be used to create a new generated field.
     */
    public UnstructuredField(Kind kind, String body) {
        super(kind);
        this.setBody(body);
    }

    @Override
    protected String generate() throws IOException {
        return new FieldGenerator().writeUnstructuredHeader(this);
    }
}
