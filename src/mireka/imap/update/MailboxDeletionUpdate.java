package mireka.imap.update;

import static mireka.imap.MailboxFlag.NON_EXISTENT;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Objects;

import mireka.imap.ParenthesizedList;
import mireka.imap.Session;
import mireka.imap.acl.Right;
import mireka.imap.parser.Generator;
import mireka.imap.store.MailboxName;
import mireka.imap.store.StoreMailboxName;

public class MailboxDeletionUpdate implements Update {
    public StoreMailboxName mailbox;
    public UnilateralResponseOption option;

    @Override
    public void send(Generator out, Session session) throws IOException {
        if (session == option.notForSession)
            return;
        if (session.user == null)
            return;
        MailboxName mbname = session.repository.parseStoreMailboxName(session.user, mailbox);
        if (session.accessControl.hasAccess(mbname, Right.LOOKUP)) {
            out.respondList(EnumSet.of(NON_EXISTENT), mbname.name,
                    new ParenthesizedList());
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(mailbox);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MailboxDeletionUpdate other = (MailboxDeletionUpdate) obj;
        return Objects.equals(mailbox, other.mailbox);
    }

}
