package mireka.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import mireka.maildata.MaildataFile;

public class StreamCopier {

    public static void writeMailDataInputStreamIntoOutputStream(
            MaildataFile maildataFile, OutputStream out) throws IOException {
        InputStream in = maildataFile.getInputStream();
        try {
            writeInputStreamIntoOutputStream(in, out);
        } finally {
            in.close();
        }
    }

    public static void writeInputStreamIntoOutputStream(InputStream in,
            OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        int cRead;
        while (-1 != (cRead = in.read(buffer))) {
            out.write(buffer, 0, cRead);
        }
    }

    public static void copyFile(File src, File dst) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(dst);
            writeInputStreamIntoOutputStream(in, out);
        } finally {
            if (out != null)
                out.close();
            if (in != null)
                in.close();
        }
    }

}
