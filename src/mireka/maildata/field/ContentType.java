package mireka.maildata.field;

import static mireka.maildata.FieldDef.*;

import java.io.IOException;

import mireka.maildata.FieldGenerator;
import mireka.maildata.HeaderField;
import mireka.maildata.MediaType;

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
