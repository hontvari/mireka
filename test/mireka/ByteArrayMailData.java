package mireka;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import mireka.util.StreamCopier;

public class ByteArrayMailData implements MailData {
    public final byte[] bytes;

    public ByteArrayMailData(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public ByteArrayInputStream getInputStream() {
        return new ByteArrayInputStream(bytes);
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        StreamCopier.writeMailDataInputStreamIntoOutputStream(this, out);
    }

    @Override
    public void close() {
        // nothing to do
    }
}
