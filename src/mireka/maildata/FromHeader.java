package mireka.maildata;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import mireka.util.CharsetUtil;

public class FromHeader extends HeaderField {

    public List<Mailbox> mailboxList = new ArrayList<>();

    @Override
    protected void writeGenerated(OutputStream out) throws IOException {
        String result = new FieldGenerator().writeFromHeader(this);
        out.write(CharsetUtil.toAsciiBytes(result));
    }
}
