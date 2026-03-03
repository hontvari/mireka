package mireka;

import java.io.ByteArrayInputStream;

import mireka.maildata.io.MaildataInputStream;
import mireka.maildata.io.MaildataReadException;
import mireka.maildata.io.MaildataSource;
import mireka.maildata.io.Range;
import mireka.maildata.io.Subsource;
import mireka.util.CharsetUtil;

public class ByteArrayMaildataSource implements MaildataSource {
    public final byte[] bytes;

    public ByteArrayMaildataSource(byte[] bytes) {
        this.bytes = bytes;
    }

    public ByteArrayMaildataSource(String maildata) {
        this.bytes = CharsetUtil.toAsciiBytes(maildata);
    }

    /**
     * {@inheritDoc}
     * 
     * Remark: the returned input stream does not use any system resources,
     * closing it is optional.
     */
    @Override
    public MaildataInputStream getInputStream(Range range) throws MaildataReadException {
        return new MaildataInputStream(new Subsource(this, range),
                new ByteArrayInputStream(bytes, (int) range.start, (int) range.length));
    }

    @Override
    public long length() throws MaildataReadException {
        return bytes.length;
    }

    @Override
    public void close() {
        // nothing to do
    }
}
