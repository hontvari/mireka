package mireka.pop.store;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.concurrent.GuardedBy;

/**
 * MaildropRepository represents a collection of maildrops within the same file
 * system directory, it maintains a list of the currently used maildrops in
 * order to make possible the synchronization of access to a maildrop.
 */
public class MaildropRepository {
    /**
     * The directory where the individual maildrop directories are residing.
     */
    private File dir;
    @GuardedBy("this")
    private Map<String, MaildropSlot> openMaildrops =
            new HashMap<String, MaildropSlot>();

    public synchronized Maildrop borrowMaildrop(String maildropName) {
        Maildrop maildrop = getOrCreateMaildrop(maildropName);
        return maildrop;
    }

    private Maildrop getOrCreateMaildrop(String maildropName) {
        MaildropSlot maildropSlot = openMaildrops.get(maildropName);
        if (maildropSlot == null) {
            maildropSlot = new MaildropSlot();
            File maildropDir = new File(dir, maildropName);
            maildropSlot.maildrop = new Maildrop(maildropName, maildropDir);
            openMaildrops.put(maildropName, maildropSlot);
        }
        maildropSlot.borrowCount++;
        return maildropSlot.maildrop;
    }

    public synchronized void releaseMaildrop(Maildrop maildrop) {
        MaildropSlot maildropSlot = openMaildrops.get(maildrop.getName());
        if (maildropSlot == null)
            throw new IllegalStateException("Maildrop is already released");
        maildropSlot.borrowCount--;
        if (maildropSlot.borrowCount < 0)
            throw new RuntimeException("Assertion failed");
        else if (maildropSlot.borrowCount == 0) {
            openMaildrops.remove(maildrop.getName());
            maildrop.checkReleasedState();
        }
    }

    /**
     * @category GETSET
     */
    public void setDir(File dir) {
        this.dir = dir;
    }

    /**
     * @category GETSET
     */
    public File getDir() {
        return dir;
    }

    private static class MaildropSlot {
        int borrowCount = 0;
        Maildrop maildrop;
    }
}
