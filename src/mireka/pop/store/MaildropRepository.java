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
    private Map<String, Maildrop> openMaildrops =
            new HashMap<String, Maildrop>();

    public synchronized Maildrop borrowMaildrop(String maildropName) {
        Maildrop maildrop = getOrCreateMaildrop(maildropName);
        return maildrop;
    }

    private Maildrop getOrCreateMaildrop(String maildropName) {
        Maildrop maildrop = openMaildrops.get(maildropName);
        if (maildrop == null) {
            File maildropDir = new File(dir, maildropName);
            maildrop = new Maildrop(maildropName, maildropDir);
            openMaildrops.put(maildropName, maildrop);
        }
        return maildrop;
    }

    public synchronized void releaseMaildrop(Maildrop maildrop) {
        if (!openMaildrops.containsKey(maildrop.getName()))
            throw new RuntimeException("Assertion failed");
        if (!maildrop.isInUse())
            openMaildrops.remove(maildrop.getName());
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

}
