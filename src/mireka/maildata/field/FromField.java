package mireka.maildata.field;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import mireka.maildata.FieldGenerator;
import mireka.maildata.HeaderField;
import mireka.maildata.Mailbox;
import mireka.util.CharsetUtil;

public class FromField extends HeaderField {

    public FromField() {
        super("From");
    }

    public List<Mailbox> mailboxList = new ArrayList<>();

    @Override
    protected void writeGenerated(OutputStream out) throws IOException {
        String result = new FieldGenerator().writeFromField(this);
        out.write(CharsetUtil.toAsciiBytes(result));
    }
}
