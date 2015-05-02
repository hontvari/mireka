/*
 * Original source code is from the SubEthaSMTP project, original author is Jeff 
 * Schnitzer.  
 */
package mireka.maildata.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.io.DeferredFileOutputStream;
import org.subethamail.smtp.io.ThresholdingOutputStream;

/**
 * DeferredFile works like a byte buffer until a certain size is reached, then
 * creates a temp file and acts like a real file. The data must be filled by
 * calling getInputStream and it can be retrieved later several times by calling
 * getInputStream().
 *
 * When this object is closed, the temporary file is deleted. You can no longer
 * call getInputStream().
 * 
 * Compared to {@link DeferredFileOutputStream} this class does not implement
 * OutputStream itself, instead it provides a method which returns an
 * OutputStream which should be used to initialize the content.
 * 
 * The advantage of this class compared to {@link DeferredFileOutputStream} is
 * that the completion of filling the initial data is explicitly marked by
 * closing the output stream. After that point any IOException is very unlikely.
 * This means that any read is almost guaranteed to not throw an IOException. In
 * contrast to this, the fist call of
 * {@link DeferredFileOutputStream#getInputStream()} may throw an exception if
 * the disk is full.
 * 
 * The disadvantage is that there is an additional step, the closing of the
 * output stream. This disadvantage is mostly eliminated by the
 * try-with-resources statement since Java 7.
 */
public class DeferredFile implements Closeable {
    private final Logger logger = LoggerFactory.getLogger(DeferredFile.class);

    /**
     * Initial size of the byte array buffer. Better to make this large to start
     * with so that we can avoid reallocs; mail messages are rarely tiny.
     */
    private static final int INITIAL_BUF_SIZE = 8192;

    /** */
    public static final String TMPFILE_PREFIX = "mireka";
    public static final String TMPFILE_SUFFIX = ".msg";

    /**
     * The byte buffer which is used until the threshold is reached.
     */
    private BetterByteArrayOutputStream byteArrayOutputStream;

    /**
     * The temporary file, if we switched to file output.
     */
    private File outFile;

    /**
     * The DeferredOutputStream created and returned by getOutputStream.
     */
    private DeferredOutputStream deferredOutputStream;

    /**
     * The input stream returned by the last call to getInputStream. It is
     * stored in this attribute in order to close() be able to close this stream
     * too. Therefore only one inputStream can be open at the same time. This is
     * not an actual restriction as of now.
     */
    private InputStream inputStream;

    /**
     * True if all data has been completely saved by flushing and closing the
     * output stream.
     */
    private boolean filled = false;

    /**
     * True if this object is closed and the temporary file is deleted.
     */
    private boolean closed;

    /**
     * The number of bytes at which to convert from a byte array to a real file.
     */
    int transitionSize = 0x10000;

    /**
     * Returns the output stream which should be used to fill this file. This
     * must be called exactly one time before the first call to
     * {@link #getInputStream()}. The returned stream must be closed before
     * that. The output stream is automatically closed if this object is closed.
     */
    public OutputStream getOutputStream() {
        // it must not be called more than once
        if (deferredOutputStream != null)
            throw new IllegalStateException();

        byteArrayOutputStream =
                new BetterByteArrayOutputStream(INITIAL_BUF_SIZE);
        deferredOutputStream = new DeferredOutputStream();
        return deferredOutputStream;
    }

    /**
     * Returns the input stream which can be used to read the file. This can be
     * called many times, but only after the file is filled by writing all data
     * into the stream returned by {@link #getOutputStream()} and closing it.
     * 
     * @return either a BetterByteArrayOutputStream or buffered FileInputStream,
     *         depending on what state we are in. The caller must close this
     *         stream. The close() operation also closes it as a last resort,
     *         but it logs a warning.
     * 
     * @throws IOException
     *             if there is an IO error, but this cannot happen in normal
     *             circumstances, because the temporary file is already on disk
     *             and closed when this method is called.
     */
    public InputStream getInputStream() throws IOException {
        if (!filled)
            throw new IllegalStateException();

        if (outFile == null) {
            inputStream = byteArrayOutputStream.getInputStream();
        } else {
            inputStream = new BufferedInputStream(new FileInputStream(outFile));
        }
        return new CloseRegisteringInputStream(inputStream);
    }

    /**
     * Deletes the temporary file if it is used and closes the output stream if
     * it is open. If the input stream is still open it logs a warning and close
     * that too.
     */
    @Override
    public void close() {
        closed = true;
        if (deferredOutputStream != null)
            deferredOutputStream.closeAbruptly();
        if (inputStream != null) {
            logger.warn("An input stream was not closed before removing the file, closing it now");
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.warn("Cannot close input stream, e");
            }
        }
        if (outFile != null)
            outFile.delete();
    }

    /**
     * DeferredOutputStream is used to fill the buffer or the file, it registers
     * when the data is completely written and close is called on this stream.
     */
    private class DeferredOutputStream extends ThresholdingOutputStream {
        private final Logger logger = LoggerFactory
                .getLogger(DeferredOutputStream.class);
        /**
         * True if this OutputStream is already closed.
         */
        private boolean closed = false;

        public DeferredOutputStream() {
            super(byteArrayOutputStream, transitionSize);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.subethamail.common.io.ThresholdingOutputStream#thresholdReached
         * (int, int)
         */
        @Override
        protected void thresholdReached(int current, int predicted)
                throws IOException {
            // Open a temp file, write the byte array version, and swap the
            // output stream to the file version.

            outFile = File.createTempFile(TMPFILE_PREFIX, TMPFILE_SUFFIX);
            OutputStream outFileStream = new FileOutputStream(outFile);

            byteArrayOutputStream.writeTo(outFileStream);
            byteArrayOutputStream = null;
            this.output = new BufferedOutputStream(outFileStream);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.subethamail.common.io.ThresholdingOutputStream#close()
         */
        @Override
        public void close() throws IOException {
            if (this.closed)
                return;
            this.closed = true;

            if (DeferredFile.this.closed)
                throw new IllegalStateException(
                        "DeferredFile closed in the meantime");
            if (filled)
                throw new IllegalStateException("Filled in the meantime");

            output.flush();
            output.close();
            filled = true;
        }

        void closeAbruptly() {
            try {
                output.close();
            } catch (IOException e) {
                logger.warn("Cannot close DeferredOutputStream", e);
            }
            closed = true;
        }
    }

    private class CloseRegisteringInputStream extends FilterInputStream {

        public CloseRegisteringInputStream(InputStream in) {
            super(in);
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                inputStream = null;
            }
        }
    }
}
