package mireka.transmission.queue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import mireka.MailData;
import mireka.util.StreamCopier;

class FileMailData implements MailData {

    private final File file;

    public FileMailData(File file) {
        this.file = file;
    }

    @Override
    public InputStream getInputStream() {
        try {
            return new FileInputStream(file);
        } catch (IOException e) {
            throw new RuntimeException("Assertion failed");
        }
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        StreamCopier.writeMailDataInputStreamIntoOutputStream(this, out);
    }

    @Override
    public void close() {
        // nothing to do
    }
}
