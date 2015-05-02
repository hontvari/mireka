package mireka;

import java.io.ByteArrayInputStream;

import mireka.maildata.io.MaildataFile;
import mireka.maildata.io.MaildataFileInputStream;
import mireka.util.CharsetUtil;

public class ByteArrayMaildataFile implements MaildataFile {
    public final byte[] bytes;

    public ByteArrayMaildataFile(byte[] bytes) {
        this.bytes = bytes;
    }

    public ByteArrayMaildataFile(String maildata) {
        this.bytes = CharsetUtil.toAsciiBytes(maildata);
    }

    /**
     * {@inheritDoc}
     * 
     * Remark: the returned input stream does not use any system resources,
     * closing it is optional.
     */
    @Override
    public MaildataFileInputStream getInputStream() {
        return new MaildataFileInputStream(new ByteArrayInputStream(bytes));
    }

    @Override
    public void close() {
        // nothing to do
    }
}
