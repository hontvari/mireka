package mireka.maildata.field;

import static mireka.maildata.parser.Kind.MIME_VERSION;

import java.io.IOException;

import mireka.maildata.HeaderField;
import mireka.maildata.parser.FieldGenerator;

public class MimeVersion extends HeaderField {

    public MimeVersion() {
        super(MIME_VERSION);
    }

    public int major;
    public int minor;

    @Override
    protected String generate() throws IOException {
        return new FieldGenerator().writeMimeVersion(this);
    }
}
