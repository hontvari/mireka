package mireka.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamCopier {

    public static void writeInputStreamIntoOutputStream(InputStream in,
            OutputStream out) throws IOException {
        byte[] buffer = new byte[0x10000];
        int cRead;
        while (-1 != (cRead = in.read(buffer))) {
            out.write(buffer, 0, cRead);
        }
    }

    public static void copyFile(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src);
                OutputStream out = new FileOutputStream(dst)) {
            writeInputStreamIntoOutputStream(in, out);
        }
    }

}
