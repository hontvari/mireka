package mireka.maildata.field;

import java.io.IOException;
import java.time.ZonedDateTime;

import mireka.maildata.HeaderField;
import mireka.maildata.parser.FieldGenerator;
import mireka.maildata.parser.Kind;

public class DateField extends HeaderField {
    public ZonedDateTime date;

    public DateField(Kind kind) {
        super(kind);
    }

    @Override
    protected String generate() throws IOException {
        FieldGenerator g = new FieldGenerator();
        g.writeDateHeader(this);
        return g.folder.toString();
    }
}
