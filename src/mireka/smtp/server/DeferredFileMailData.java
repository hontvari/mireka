package mireka.smtp.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.subethamail.smtp.io.DeferredFileOutputStream;

import mireka.MailData;
import mireka.util.StreamCopier;

public class DeferredFileMailData implements MailData {
    private final DeferredFileOutputStream deferredFileOutputStream;

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
