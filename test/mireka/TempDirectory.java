package mireka;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import mireka.transmission.queue.DirectoryListSpeedTest;

import org.junit.After;
import org.junit.Before;

public class TempDirectory {

    protected File directory;

    public TempDirectory() {
        super();
    }

    @Before
    public void createTestDirectory() {
        String tempDirProperty = System.getProperty("java.io.tmpdir");
        File tempDir = new File(tempDirProperty);
        File dir;
        boolean created = false;
        do {
            String dirName =
                    DirectoryListSpeedTest.class.getSimpleName() + "-"
                            + Math.random();
            dir = new File(tempDir, dirName);
            created = dir.mkdir();
        } while (!created);
        directory = dir;
    }

    @After
    public void deleteTestDirectory() {
        for (File file : directory.listFiles()) {
            file.delete();
        }
        directory.delete();
    }

    public String textFileContent(String relativePath) throws IOException {
        File file = new File(directory, relativePath);
        return textFileContent(file);
    }

    public static String textFileContent(File file) throws IOException {
        long size = file.length();
        InputStreamReader reader =
                new InputStreamReader(new FileInputStream(file), "UTF-8");
        char[] buffer = new char[(int) size];
        try {
            int count = reader.read(buffer);
            return new String(buffer, 0, count);
        } finally {
            reader.close();
        }
    }

    public void writeText(String relativePath, String text) throws IOException {
        writeText(new File(directory, relativePath), text);
    }

    public static void writeText(File file, String text) throws IOException {
        OutputStreamWriter writer =
                new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
        writer.write(text);
        writer.close();
    }
}