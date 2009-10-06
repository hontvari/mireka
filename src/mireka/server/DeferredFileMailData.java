package mireka.server;

import java.io.IOException;
import java.io.InputStream;

import org.subethamail.smtp.io.DeferredFileOutputStream;

import mireka.filter.MailData;

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

    public void close() throws IOException {
        deferredFileOutputStream.close();
    }
}
