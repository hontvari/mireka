package mireka.smtp.server;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * SmtpDataInputStream is a wrapper around the network connection to the client,
 * it throws {@link SmtpDataReadException} on network read errors, so connection
 * errors will be distinguishable from local IO errors.
 */
public class SmtpDataInputStream extends FilterInputStream {

    public SmtpDataInputStream(InputStream in) {
        super(in);
    }

    @Override
    public int read() throws SmtpDataReadException {
        try {
            return super.read();
        } catch (IOException e) {
            throw new SmtpDataReadException(e);
        }
    }

    @Override
    public int read(byte[] b) throws SmtpDataReadException {
        try {
            return super.read(b);
        } catch (IOException e) {
            throw new SmtpDataReadException(e);
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws SmtpDataReadException {
        try {
            return super.read(b, off, len);
        } catch (IOException e) {
            throw new SmtpDataReadException(e);
        }
    }

    @Override
    public long skip(long n) throws SmtpDataReadException {
        try {
            return super.skip(n);
        } catch (IOException e) {
            throw new SmtpDataReadException(e);
        }
    }

    @Override
    public int available() throws SmtpDataReadException {
        try {
            return super.available();
        } catch (IOException e) {
            throw new SmtpDataReadException(e);
        }
    }

    @Override
    public void close() throws SmtpDataReadException {
        try {
            super.close();
        } catch (IOException e) {
            throw new SmtpDataReadException(e);
        }
    }

    @Override
    public synchronized void reset() throws SmtpDataReadException {
        try {
            super.reset();
        } catch (IOException e) {
            throw new SmtpDataReadException(e);
        }
    }

}
