package mireka.maildata.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * InputStream wrapper which maintains position information, provides pushback functionality, limits
 * the stream to a specific length, throws runtime exception instead of checked IOException.
 * 
 * Read operations throw unchecked {@link MaildataReadException} exception instead of IOException.
 * See {@link MaildataReadException} for the reasons.
 */
public class MaildataInputStream extends PushbackInputStream {
    public Subsource source;

    /**
     * The boundaries of this substream within the top level stream.
     */
    private Range range;

    /**
     * position of the next read byte relative to the start of the range. The absolute position in
     * the source file is {@link Range#start} + {@link #position}
     */
    public long position = 0;

    /**
     * @param source the origin of the supplied stream
     * @param in a stream which is positioned to the first byte of the supplied source.
     */
    public MaildataInputStream(Subsource source, InputStream in) {
        super(in);
        this.range = source.range;
        this.source = source;
    }

    @Override
    public int read() throws MaildataReadException {
        try {
            if (position >= range.length)
                return -1;
            int b = in.read();
            if (b == -1)
                throw new EOFException();
            position++;
            return b;
        } catch (IOException e) {
            throw new MaildataReadException(e);
        }
    }

    @Override
    public int read(byte[] b) throws MaildataReadException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len)
            throws MaildataReadException {
        try {
            if (position + len >= range.length)
                len = (int) (range.length - position);
            int c = super.read(b, off, len);
            if (c == -1)
                throw new EOFException();
            position += c;
            return c;
        } catch (IOException e) {
            throw new MaildataReadException(e);
        }
    }

    @Override
    public long skip(long n) throws MaildataReadException {
        try {
            long c = super.skip(n);
            position += c;
            return c;
        } catch (IOException e) {
            throw new MaildataReadException(e);
        }
    }

    @Override
    public void unread(int b) throws IOException {
        position--;
        super.unread(b);
    }

    @Override
    public void unread(byte[] b, int off, int len) throws IOException {
        position -= len;
        super.unread(b, off, len);
    }

    @Override
    public void unread(byte[] b) throws IOException {
        position -= b.length;
        super.unread(b);
    }

    public void skipToEnd() {
        // skip may return 0 even before the end of stream.
        skip(range.length - position);
        byte[] buf = new byte[4096];
        while (read(buf) != -1)
            ;
    }

    @Override
    public void close() throws MaildataReadException {
        try {
            super.close();
        } catch (IOException e) {
            throw new MaildataReadException(e);
        }
    }

}
