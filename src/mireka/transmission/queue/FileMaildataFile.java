package mireka.transmission.queue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import mireka.maildata.io.MaildataInputStream;
import mireka.maildata.io.MaildataReadException;
import mireka.maildata.io.MaildataSource;
import mireka.maildata.io.Range;
import mireka.maildata.io.Subsource;

class FileMaildataFile implements MaildataSource {

    private final File file;
    private long length;

    public FileMaildataFile(File file) {
        this.file = file;
        this.length = file.length();
    }

    @Override
    public MaildataInputStream getInputStream(Range range) {
        try {
            FileInputStream in = new FileInputStream(file);
            in.skip(range.start);
            return new MaildataInputStream(new Subsource(this, range), in);
        } catch (IOException e) {
            throw new RuntimeException("Assertion failed");
        }
    }

    @Override
    public long length() throws MaildataReadException {
        return length;
    }

    @Override
    public void close() {
        // nothing to do
    }
}
