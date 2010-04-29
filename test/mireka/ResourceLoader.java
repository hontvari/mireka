package mireka;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ResourceLoader {

    /**
     * loads a mail from a file on the class path
     * 
     * @param caller
     *            it gives the base package, if a relative name is supplied
     * @param name
     *            either an absolute or a relative name, for example /mail.eml
     */
    public static byte[] loadResource(Class<?> caller, String name) {
        InputStream resouceStream = caller.getResourceAsStream(name);
        if (resouceStream == null)
            throw new IllegalArgumentException(name + " not found");
        try {
            ByteArrayOutputStream inMemoryOut = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int cBytesRead;
            while (-1 != (cBytesRead = resouceStream.read(buffer))) {
                inMemoryOut.write(buffer, 0, cBytesRead);
            }
            return inMemoryOut.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                resouceStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
