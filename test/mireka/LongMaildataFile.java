package mireka;

import java.io.IOException;
import java.io.InputStream;

import mireka.maildata.io.MaildataInputStream;
import mireka.maildata.io.MaildataReadException;
import mireka.maildata.io.MaildataSource;
import mireka.maildata.io.Range;
import mireka.maildata.io.Subsource;

public class LongMaildataFile implements MaildataSource {
    private static final int GENERATED_LENGTH = 500 * 1000 * 1000;
    private byte[] heading;

    public LongMaildataFile() {
        this.heading = ResourceLoader.loadResource(getClass(), "emptyMail.eml");
    }

    @Override
    public long length() throws MaildataReadException {
        return heading.length + GENERATED_LENGTH;
    }

    @Override
    public void close() {
        // do nothing
    }

    @Override
    public MaildataInputStream getInputStream(Range range) throws MaildataReadException {
        try {
            InputStreamGenerator in = new InputStreamGenerator(heading);
            in.skip(range.start);
            return new MaildataInputStream(new Subsource(this, range), in);
        } catch (IOException e) {
            throw new MaildataReadException(e);
        }
    }

    private static class InputStreamGenerator extends InputStream {
        private final byte[] heading;
        private Part part = Part.Heading;
        private int i = 0;

        public InputStreamGenerator(byte[] heading) {
            this.heading = heading;
        }

        @Override
        public int read() throws IOException {
            int result;
            switch (part) {
            case Heading:
                result = heading[i++];
                if (i == heading.length) {
                    part = Part.Body;
                    i = 0;
                }
                return result;
            case Body:
                int iColumn = i++ % 80;
                if (iColumn == 78)
                    result = '\r';
                else if (iColumn == 79)
                    result = '\n';
                else
                    result = 'X';
                if (i == GENERATED_LENGTH) {
                    part = Part.Eof;
                    i = 0;
                }
                return result;
            case Eof:
                return -1;
            default:
                throw new RuntimeException();
            }
        }

        private static enum Part {
            Heading, Body, Eof;
        }

    }
}
