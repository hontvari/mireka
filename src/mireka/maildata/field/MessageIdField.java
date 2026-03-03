package mireka.maildata.field;

import java.io.IOException;

import mireka.maildata.HeaderField;
import mireka.maildata.parser.FieldGenerator;
import mireka.maildata.parser.Kind;

public class MessageIdField extends HeaderField {
    public String msgId;

    public MessageIdField(Kind kind) {
        super(kind);
    }

    @Override
    protected String generate() throws IOException {
        FieldGenerator g = new FieldGenerator();
        g.writeMessageIdHeader(this);
        return g.folder.toString();
    }

}
