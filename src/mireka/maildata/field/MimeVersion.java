package mireka.maildata.field;

import java.io.IOException;

import mireka.maildata.FieldGenerator;
import mireka.maildata.HeaderField;

public class MimeVersion extends HeaderField {
    public int major;
    public int minor;

    @Override
    protected String generate() throws IOException {
        return new FieldGenerator().writeMimeVersion(this);
    }
}
