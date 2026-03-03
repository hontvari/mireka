package mireka.imap.acl;

import mireka.imap.store.MailboxName;

public interface AccessControl {
    boolean hasAccess(MailboxName mailbox, Right operation);

    default void checkAccess(MailboxName mbname, Right operation) throws NoPermissionException {
        if (!hasAccess(mbname, operation))
            throw new NoPermissionException();
    }
}
