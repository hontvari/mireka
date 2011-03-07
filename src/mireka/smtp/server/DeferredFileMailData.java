package mireka.smtp.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import mireka.MailData;
import mireka.util.StreamCopier;

import org.subethamail.smtp.io.DeferredFileOutputStream;

/**
 * DeferredFileMailData stores message content in memory if it is short or in a
 * temporary file if it is long.
 */
public class DeferredFileMailData implements MailData {
    private final DeferredFileOutputStream deferredFileOutputStream;

    /**
     * Constructs a new DeferredFileMailData so that it contains the message
     * content residing in the specified stream.
     * 
     * @param deferredFileOutputStream
     *            The stream containing the message content.
     */
    public DeferredFileMailData(
            DeferredFileOutputStream deferredFileOutputStream) {
        this.deferredFileOutputStream = deferredFileOutputStream;
    }

    @Override
    public InputStream getInputStream() {
        try {
            return deferredFileOutputStream.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        StreamCopier.writeMailDataInputStreamIntoOutputStream(this, out);
    }

    @Override
    public void dispose() {
        try {
            deferredFileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(
                    "Unexpected exception while closing stream", e);
        }
    }
}
