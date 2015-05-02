package mireka.maildata.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * MaildataFileInputStream reads from a MaildataFile and provides read
 * operations which throw unchecked MaildataFileReadException exception instead
 * of IOException. See {@link MaildataFileReadException} for the reasons.
 */
public class MaildataFileInputStream extends FilterInputStream {

    public MaildataFileInputStream(InputStream in) {
        super(in);
    }

    @Override
    public int read() throws MaildataFileReadException {
        try {
            return in.read();
        } catch (IOException e) {
            throw new MaildataFileReadException(e);
        }
    }

    @Override
    public int read(byte[] b) throws MaildataFileReadException {
        try {
            return in.read(b);
        } catch (IOException e) {
            throw new MaildataFileReadException(e);
        }
    }

    @Override
    public int read(byte[] b, int off, int len)
            throws MaildataFileReadException {
        try {
            return super.read(b, off, len);
        } catch (IOException e) {
            throw new MaildataFileReadException(e);
        }
    }

    @Override
    public long skip(long n) throws MaildataFileReadException {
        try {
            return super.skip(n);
        } catch (IOException e) {
            throw new MaildataFileReadException(e);
        }
    }

    @Override
    public void close() throws MaildataFileReadException {
        try {
            super.close();
        } catch (IOException e) {
            throw new MaildataFileReadException(e);
        }
    }
}
