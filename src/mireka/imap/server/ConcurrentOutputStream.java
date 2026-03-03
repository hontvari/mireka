package mireka.imap.server;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Anything written out between a lock and an unlock call appears as a continuous data which is not
 * mixed with data of other threads.
 * 
 * It is used to separate the output of multiple concurrently running commands.
 */
public class ConcurrentOutputStream extends FilterOutputStream {
    private static final int LENGTH = 4096;

    public final ReentrantLock lock = new ReentrantLock();
    private final ProtocolLogger protocolLogger;

    public ConcurrentOutputStream(OutputStream out, ProtocolLogger protocolLogger) {
        super(out);
        this.protocolLogger = protocolLogger;
    }

    /**
     * Returns a stream which first buffers the output, then if the buffer is full it locks the main
     * outputstream and flushes the buffer. It holds the lock until the next flush call or until the
     * stream is closed. The returned stream should be used by only one thread at a time.
     */
    public Substream createSubstream() {
        return new Substream();
    }

    @Override
    public void write(int b) throws IOException {
        protocolLogger.logServerStream(b);
        out.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        protocolLogger.logServerStream(b);
        out.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        protocolLogger.logServerStream(b, off, len);
        out.write(b, off, len);
    }

    public class Substream extends FilterOutputStream {
        private byte[] buffer = new byte[LENGTH];
        private int count;
        private boolean locking;

        public Substream() {
            super(ConcurrentOutputStream.this);
        }

        @Override
        public void write(int b) throws IOException {
            if (locking) {
                out.write(b);
            } else {
                if (count < LENGTH) {
                    buffer[count++] = (byte) b;
                } else {
                    lock.lock();
                    locking = true;
                    out.write(buffer);
                    out.write(b);
                    count = 0;
                }
            }
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (locking) {
                out.write(b, off, len);
            } else {
                if (count + len <= LENGTH) {
                    System.arraycopy(b, off, buffer, count, len);
                    count += len;
                } else {
                    lock.lock();
                    locking = true;
                    out.write(buffer, 0, count);
                    out.write(b, off, len);
                    count = 0;
                }
            }
        }

        /**
         * It writes the content of the buffer to the underlying output stream, and if the
         * underlying stream was locked by this instance, than it releases the lock. It should be
         * called when other thread may start to write to the underlying stream.
         */
        @Override
        public void flush() throws IOException {
            out.write(buffer, 0, count);
            count = 0;
            out.flush();
            if (locking) {
                lock.unlock();
                locking = false;
            }
        }

        @Override
        public void close() throws IOException {
            flush();
        }
    }
}
