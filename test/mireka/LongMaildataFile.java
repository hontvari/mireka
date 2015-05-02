package mireka;

import java.io.IOException;
import java.io.InputStream;

import mireka.maildata.io.MaildataFile;
import mireka.maildata.io.MaildataFileInputStream;
import mireka.maildata.io.MaildataFileReadException;

public class LongMaildataFile implements MaildataFile {

    @Override
    public void close() {
        // do nothing
    }

    @Override
    public MaildataFileInputStream getInputStream()
            throws MaildataFileReadException {
        return new MaildataFileInputStream(new InputStreamGenerator(
                ResourceLoader.loadResource(getClass(), "emptyMail.eml")));
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
                if (i == 500 * 1000 * 1000) {
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
