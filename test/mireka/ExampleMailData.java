package mireka;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ExampleMailData {
    public static ByteArrayMaildataFile simple() {
        return fromResource(ExampleMailData.class, "simpleMail.eml");
    }

    /**
     * loads a mail from a file on the class path
     * 
     * @param caller
     *            it gives the base package, if a relative name is supplied
     * @param name
     *            either an absolute or a relative name, for example /mail.eml
     */
    public static ByteArrayMaildataFile fromResource(Class<?> caller, String name) {
        return new ByteArrayMaildataFile(ResourceLoader.loadResource(caller, name));
    }

    public static ByteArrayMaildataFile mail4k() {
        try {
            int requiredSize = 4096;
            ByteArrayOutputStream buffer =
                    new ByteArrayOutputStream(requiredSize);
            ByteArrayMaildataFile simpleMail = simple();
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

            return new ByteArrayMaildataFile(buffer.toByteArray());
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
