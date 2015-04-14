package mireka.maildata;

import java.io.IOException;
import java.io.OutputStream;

public class UnparsedHeaderField extends HeaderField {

    @Override
    protected void writeGenerated(OutputStream out) throws IOException {
        throw new RuntimeException("Assertion failed");
    }

}
