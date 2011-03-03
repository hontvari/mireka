package mireka.pop.store;

import mireka.pop.Pop3Exception;

/**
 * Indicates that the locking of the maildrop is failed, because it is already
 * locked.
 */
public class MaildropLockedException extends Pop3Exception {
    private static final long serialVersionUID = 1346569606845009094L;

    public MaildropLockedException() {
        super("IN-USE", "Maildrop already locked");
    }
}
