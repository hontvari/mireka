package mireka.transmission.queue;

import static org.junit.Assert.*;

import java.io.File;

import mireka.ExampleAddress;
import mireka.ExampleMail;
import mireka.transmission.Mail;

import org.junit.Test;

public class FileDirStoreTest extends TempDirectory {
    @Test
    public void testSave() throws Exception {
        FileDirStore store = new FileDirStore(directory, 10);
        Mail mail = ExampleMail.simple();
        store.save(mail);

        FileDirStore restartedStore = new FileDirStore(directory, 10);
        MailName[] mailNames =
                restartedStore.initializeAndQueryMailNamesOrderedBySchedule();
        assertEquals(1, mailNames.length);
    }

    @Test
    public void testSaveWithSameDate() throws Exception {
        FileDirStore store = new FileDirStore(directory, 10);

        Mail mail = ExampleMail.simple();
        mail.from = ExampleAddress.JOHN;
        store.save(mail);
        mail.from = ExampleAddress.JANE;
        store.save(mail);

        FileDirStore restartedStore = new FileDirStore(directory, 10);
        MailName[] mailNames =
                restartedStore.initializeAndQueryMailNamesOrderedBySchedule();
        assertEquals(2, mailNames.length);
    }

    @Test
    public void testRead() throws Exception {
        FileDirStore store = new FileDirStore(directory, 10);

        Mail mailStored = ExampleMail.simple();
        MailName mailName = store.save(mailStored);

        Mail mailRead = store.read(mailName);
        assertEquals(mailStored.from, mailRead.from);
    }

    @Test
    public void testDelete() throws Exception {
        FileDirStore store = new FileDirStore(directory, 10);

        Mail mailStored = ExampleMail.simple();
        MailName mailName = store.save(mailStored);
        store.delete(mailName);

        FileDirStore restartedStore = new FileDirStore(directory, 10);
        MailName[] mailNames =
                restartedStore.initializeAndQueryMailNamesOrderedBySchedule();
        assertEquals(0, mailNames.length);
    }

    @Test
    public void testMoveToErrorDir() throws Exception {
        FileDirStore store = new FileDirStore(directory, 10);

        Mail mailStored = ExampleMail.simple();
        MailName mailName = store.save(mailStored);
        store.moveToErrorDir(mailName);

        FileDirStore restartedStore = new FileDirStore(directory, 10);
        MailName[] mailNames =
                restartedStore.initializeAndQueryMailNamesOrderedBySchedule();
        assertEquals(0, mailNames.length);
        File errorDir = new File(directory, "error");
        assertTrue(errorDir.isDirectory());
        assertEquals(2, errorDir.list().length);
    }

    @Test(expected = QueueStorageException.class)
    public void testFull() throws Exception {
        FileDirStore store = new FileDirStore(directory, 1);

        Mail mail = ExampleMail.simple();
        store.save(mail);
        store.save(mail);
    }

    /**
     * This test checks a previous bug.
     */
    @Test
    public void testSizeMaintained() throws Exception {
        FileDirStore store = new FileDirStore(directory, 1);
        Mail mail = ExampleMail.simple();

        MailName lastName = store.save(mail);
        store.delete(lastName);

        // this must not throw an exception, because the first mail was deleted,
        // so there is still place for a new mail
        store.save(mail);
    }
}
