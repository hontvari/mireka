package mireka;

import java.io.ByteArrayInputStream;

import mireka.filter.MailData;

public class ByteArrayMailData implements MailData {
    public final byte[] bytes;

    public ByteArrayMailData(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public ByteArrayInputStream getInputStream() {
        return new ByteArrayInputStream(bytes);
    }
}
