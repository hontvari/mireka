package mireka.imap.command;

import static mireka.imap.Completion.ok;
import static mireka.imap.SessionState.*;

import java.io.IOException;
import java.util.EnumSet;

import mireka.imap.Completion;
import mireka.imap.ImapException;
import mireka.imap.ParenthesizedList;
import mireka.imap.ResponseCode;
import mireka.imap.Session;
import mireka.imap.SessionState;
import mireka.imap.acl.Right;
import mireka.imap.parser.CommandParser;
import mireka.imap.store.Mailbox;
import mireka.imap.store.Mailbox.Status;
import mireka.imap.store.MailboxHierarchy;
import mireka.imap.store.MailboxName;

/**
 * Corresponds to both the SELECT and EXAMINE command.
 */
public class SelectExamineCommand extends Command {
    /**
     * true if an EXAMINE command is executed, false if a SELECT
     */
    boolean isExamine;
    private String name;

    public SelectExamineCommand(Session session, CommandParser parser) {
        super(session, parser);
    }

    @Override
    public void parse() throws IOException, ImapException {
        isExamine = parser.command.equals("EXAMINE");

        parser.take(' ');
        name = parser.parseAstring("mailbox");
    }

    @Override
    public Completion execute() throws IOException, ImapException {
        if (session.state == SELECTED) {
            session.resetAuthenticatedState();
            out.respondUntaggedOk(ResponseCode.of("CLOSED"), "Previous mailbox is now closed");
        }

        MailboxName mbname = repository().parseMailboxName(session.user, name);
        MailboxHierarchy hierarchy = hierarchy(mbname.namespace);
        Mailbox mailbox = hierarchy.mailbox(mbname);
        session.setSelectedState(mbname, mailbox, isExamine);
        out.respondFlags(mailbox.getFlags());
        Status status = mailbox.status();
        out.respondExists(status.count);
        out.respondList(hierarchy.mailboxAttributes(mbname), mbname.name, new ParenthesizedList());
        out.respondUntaggedOk(ResponseCode.of("UIDNEXT " + status.uidnext), "Predicted next UID");
        out.respondUntaggedOk(ResponseCode.of("UIDVALIDITY " + status.uidvalidity), "UIDs valid");
        boolean readonly = isExamine || !hasWriteAccess(mbname);
        return ok(ResponseCode.of(readonly ? "READ-ONLY" : "READ-WRITE"));
    }

    @Override
    public Completion asyncExecute() throws IOException, ImapException {
        throw new IllegalStateException();
    }
    
    /**
     * Returns true if the user has a least one of the "i", "e", and "shared flag rights".
     */
    private boolean hasWriteAccess(MailboxName mbname) {
        // assuming that the mailbox has shared \Seen, \Deleted, and at least one other shared flag.
        return accessControl().hasAccess(mbname, Right.INSERT) ||
                accessControl().hasAccess(mbname, Right.EXPUNGE)
                || accessControl().hasAccess(mbname, Right.SEEN)
                || accessControl().hasAccess(mbname, Right.WRITE)
                || accessControl().hasAccess(mbname, Right.DELETE_MESSAGE);
    }

    @Override
    public EnumSet<SessionState> allowed() {
        return EnumSet.of(AUTHENTICATED, SELECTED);
    }

}
