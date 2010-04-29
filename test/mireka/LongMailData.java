package mireka;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LongMailData implements MailData {

    @Override
    public void dispose() {
        // do nothing
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new InputStreamGenerator(ResourceLoader.loadResource(getClass(),
                "emptyMail.eml"));
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        InputStream in = getInputStream();
        byte[] buffer = new byte[8192];
        int cRead;
        while (-1 != (cRead = in.read(buffer))) {
            out.write(buffer, 0, cRead);
        }
        in.close();
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
