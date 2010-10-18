package mireka.filter.spf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PrependingInputStream extends InputStream {
    private final ByteArrayInputStream header;
    private final InputStream in;

    public PrependingInputStream(byte[] header, InputStream in) {
        this.header = new ByteArrayInputStream(header);
        this.in = in;
    }

    @Override
    public int available() throws IOException {
        return header.available() + in.available();
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @Override
    public int read() throws IOException {
        if (header.available() > 0)
            return header.read();
        else
            return in.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (header.available() > 0) {
            int countRead = header.read(b, off, len);
            if (countRead < len) {
                // We need to add a little extra from the normal stream
                int remainder = len - countRead;
                int additionalRead = in.read(b, countRead, remainder);

                return countRead + additionalRead;
            } else {
                return countRead;
            }
        } else {
            return in.read(b, off, len);
        }
    }

    @Override
    public long skip(long n) throws IOException {
        if (header.available() > 0) {
            long countSkipped = header.skip(n);
            if (countSkipped < n) {
                long remainder = n - countSkipped;
                long additionalSkips = in.skip(remainder);
                if (additionalSkips < 0)
                    return countSkipped;
                else
                    return countSkipped + additionalSkips;
            } else {
                return countSkipped;
            }
        } else {
            return in.skip(n);
        }
    }
}
