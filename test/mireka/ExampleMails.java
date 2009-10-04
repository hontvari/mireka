package mireka;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ExampleMails {
    public static ByteArrayMailData simple() {
        return fromResource(ExampleMails.class, "simpleMail.eml");
    }

    /**
     * loads a mail from a file on the class path
     * 
     * @param caller
     *            it gives the base package, if a relative name is supplied
     * @param name
     *            either an absolute or a relative name, for example /mail.eml
     */
    public static ByteArrayMailData fromResource(Class<?> caller, String name) {
        InputStream resouceStream = caller.getResourceAsStream(name);
        try {
            ByteArrayOutputStream inMemoryOut = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int cBytesRead;
            while (-1 != (cBytesRead = resouceStream.read(buffer))) {
                inMemoryOut.write(buffer, 0, cBytesRead);
            }
            return new ByteArrayMailData(inMemoryOut.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (resouceStream != null) {
                try {
                    resouceStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static ByteArrayMailData mail4k() {
        try {
            int requiredSize = 4096;
            ByteArrayOutputStream buffer =
                    new ByteArrayOutputStream(requiredSize);
            ByteArrayMailData simpleMail = simple();
            buffer.write(simpleMail.bytes);
            byte[] line = create100OctetLine();
            int lineCount = (requiredSize - simpleMail.bytes.length) / 100;
            for (int i = 0; i < lineCount; i++) {
                buffer.write(line);
            }

            int remainingOctets = requiredSize - buffer.size();
            for (int i = 0; i < remainingOctets; i++) {
                buffer.write(66);
            }

            return new ByteArrayMailData(buffer.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] create100OctetLine() {
        byte[] line = new byte[100];
        for (int i = 0; i < 98; i++) {
            line[i] = 65;
        }
        line[98] = 13;
        line[99] = 10;
        return line;
    }
}
