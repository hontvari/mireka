package mireka.pop.store;

import static org.junit.Assert.*;

import java.io.IOException;

import mireka.TempDirectory;

import org.junit.Test;

public class UidManagerTest extends TempDirectory {

    @Test
    public void testCreateInitialUidFile() throws InvalidUidFileException {
        UidManager uidManager = new UidManager(directory);
        uidManager.createInitialUidFile();

        uidManager = new UidManager(directory);
        uidManager.init();
        assertEquals(1, uidManager.allocateUid());

        uidManager = new UidManager(directory);
        uidManager.init();
        assertEquals(2, uidManager.allocateUid());
    }

    @Test
    public void testPowerOutage() throws InvalidUidFileException, IOException {
        writeText("temp.uid.txt", "5+");
        UidManager uidManager = new UidManager(directory);
        uidManager.init();
        assertEquals(6, uidManager.allocateUid());
    }
}
