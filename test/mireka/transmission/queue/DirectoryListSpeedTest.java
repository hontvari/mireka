package mireka.transmission.queue;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class DirectoryListSpeedTest extends TempDirectory {
    private static final int ENTRY_COUNT = 10000;

    @Before
    public void createTestFiles() throws IOException {
        for (int i = 0; i < ENTRY_COUNT; i++) {
            String baseName = Math.random() + "-" + i;
            new File(directory, baseName + ".properties").createNewFile();
            new File(directory, baseName + ".eml").createNewFile();
        }
    }

    @Test(timeout = 1000)
    public void measure() {
        File[] files = directory.listFiles();
        assertTrue(files.length == ENTRY_COUNT * 2);
    }
}
