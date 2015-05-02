package mireka.transmission.queue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import mireka.maildata.io.MaildataFile;
import mireka.maildata.io.MaildataFileInputStream;

class FileMaildataFile implements MaildataFile {

    private final File file;

    public FileMaildataFile(File file) {
        this.file = file;
    }

    @Override
    public MaildataFileInputStream getInputStream() {
        try {
            return new MaildataFileInputStream(new FileInputStream(file));
        } catch (IOException e) {
            throw new RuntimeException("Assertion failed");
        }
    }

    @Override
    public void close() {
        // nothing to do
    }
}
