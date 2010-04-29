package mireka.transmission.queue;

import java.io.File;

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

}