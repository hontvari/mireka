package mireka.maildata.field;

import static mireka.maildata.parser.Kind.CONTENT_TYPE;

import java.io.IOException;

import mireka.maildata.HeaderField;
import mireka.maildata.parser.FieldGenerator;
import mireka.maildata.type.MediaType;

public class ContentType extends HeaderField {

    public MediaType mediaType;

    public ContentType() {
        super(CONTENT_TYPE);
    }

    @Override
    protected String generate() throws IOException {
        return new FieldGenerator().writeContentType(this);
    }

}
