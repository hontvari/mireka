package mireka.filter.builtin;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class ThresholdingInputStream extends FilterInputStream {

    /** When to trigger */
    int threshold;

    /** Number of bytes read so far */
    int read = 0;

    /**
     * Number of bytes read when the {@link #mark(int)} method was called last
     * time
     */
    int markPosition = 0;

    boolean thresholdReached = false;

    /**
     */
    public ThresholdingInputStream(InputStream in, int thresholdBytes) {
        super(in);
        this.in = in;
        this.threshold = thresholdBytes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.FilterInputStream#mark(int)
     */
    @Override
    public synchronized void mark(int readlimit) {
        super.mark(readlimit);
        this.markPosition = read;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#read(byte[], int, int)
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int actualLength = this.in.read(b, off, len);
        if (actualLength == -1)
            return -1;
        this.read += actualLength;
        this.checkThreshold();
        return actualLength;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#read(byte[])
     */
    @Override
    public int read(byte[] b) throws IOException {
        int actualLength = this.in.read(b);
        if (actualLength == -1)
            return -1;
        this.read += actualLength;
        this.checkThreshold();
        return actualLength;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.InputStream#read()
     */
    @Override
    public int read() throws IOException {
        int ch = this.in.read();
        if (ch == -1)
            return -1;
        this.read++;
        this.checkThreshold();
        return ch;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.FilterInputStream#reset()
     */
    @Override
    public synchronized void reset() throws IOException {
        super.reset();
        this.read = markPosition;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.FilterInputStream#skip(long)
     */
    @Override
    public long skip(long n) throws IOException {
        long skipped = this.in.skip(n);
        if (skipped > Integer.MAX_VALUE)
            throw new IOException(
                    "Skipping more then MAX_INT bytes are not supported");
        if (skipped <= 0)
            return skipped;
        int actualLength = (int) skipped;
        this.read += actualLength;
        this.checkThreshold();
        return skipped;
    }

    /**
     * Checks whether reading count bytes would cross the limit.
     */
    protected void checkThreshold() throws IOException {
        if (!this.thresholdReached && this.read > this.threshold) {
            this.thresholdReached(this.read);
            this.thresholdReached = true;
        }
    }

    /**
     * @return the current threshold value.
     */
    public int getThreshold() {
        return this.threshold;
    }

    /**
     * Called when the threshold is about to be exceeded. This isn't exact; it's
     * called whenever a write would occur that would cross the amount. Once it
     * is called, it isn't called again.
     * 
     * @param current
     *            is the current number of bytes that have been written
     */
    abstract protected void thresholdReached(int current) throws IOException;

}
