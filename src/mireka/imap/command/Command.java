package mireka.imap.command;

import java.io.IOException;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mireka.imap.Completion;
import mireka.imap.ImapException;
import mireka.imap.MessageFlag;
import mireka.imap.MessageFlagSet;
import mireka.imap.Namespace;
import mireka.imap.Session;
import mireka.imap.SessionState;
import mireka.imap.UnavailableException;
import mireka.imap.acl.AccessControl;
import mireka.imap.acl.Right;
import mireka.imap.parser.CharClass;
import mireka.imap.parser.CommandParser;
import mireka.imap.parser.CommandSyntaxException;
import mireka.imap.parser.Generator;
import mireka.imap.store.Mailbox;
import mireka.imap.store.MailboxHierarchy;
import mireka.imap.store.MailboxName;
import mireka.imap.store.Repository;
import mireka.imap.store.StoreTransaction;
import mireka.imap.store.Transaction;

/**
 * A command is responsible for the parsing and execution of a IMAP command received from the
 * client.
 */
public abstract class Command {
    private final Logger logger = LoggerFactory.getLogger(Command.class);
    protected final Session session;
    protected final CommandParser parser;
    public final String tag;
    protected final Generator out;
    /**
     * The root command, in case of the UID command it references the UID command, and not the
     * subcommand, in other cases it references this.
     */
    public Command parent = this;

    public Command(Session session, CommandParser parser) {
        this.session = session;
        this.parser = parser;
        tag = parser.tag;
        out = new Generator(session.connection.outputStream.createSubstream(),
                session.protocolLogger);
    }

    /**
     * Parses the command, except the ending EOL.
     */
    public abstract void parse() throws IOException, ImapException;

    /**
     * Executes the command, after parsing arguments if necessary.
     * 
     * A generic store {@link Transaction} and an IMAP {@link StoreTransaction} is already started
     * before this method is called. The session state is also checked if the command is allowed in
     * that state.
     */
    public abstract Completion execute() throws IOException, ImapException;

    public abstract Completion asyncExecute() throws IOException, ImapException;

    public abstract EnumSet<SessionState> allowed();

    /**
     * Returns true if this command can be started in parallel with the supplied list of other
     * commands already running.
     * 
     * This default implementation only allows the processing of this command if no other command is
     * running.
     */
    public boolean isStartable(LinkedHashSet<Command> running) {
        logger.trace("Commands in progress: {}", running);
        boolean isStartable = running.isEmpty();
        // To help later optimization, log waiting.
        if (!isStartable)
            logger.debug("Command is waiting for completion of other commands. "
                    + "Waiting: {}. Running: {}", this, running);
        return isStartable;
    }

    /**
     * The returned string contains the tag, command, subcommand.
     */
    @Override
    public String toString() {
        return parser.command + (parser.subcommand != null ? "/" + parser.subcommand : "")
                + " [tag=" + tag + "]";
    }

    protected Repository repository() {
        return session.server.getRepository();
    }

    protected AccessControl accessControl() {
        return session.server.accessControl;
    }

    protected MailboxHierarchy hierarchy(Namespace ns) {
        return repository().userMailboxHierarchy(session.user, ns);
    }

    protected Mailbox mailbox() {
        return session.mailbox;
    }

    protected boolean is(CharClass charclass) {
        return parser.is(charclass);
    }

    protected boolean is(char c) {
        return parser.is(c);
    }

    protected int next() {
        return parser.next();
    }

    protected int take() throws IOException {
        return parser.take();
    }

    protected int take(CharClass charclass) throws CommandSyntaxException, IOException {
        return parser.take(charclass);
    }

    protected int take(char c) throws CommandSyntaxException, IOException {
        return parser.take(c);
    }

    /**
     * Checks permission on the selected mailbox, also considering if the mailbox was opened in
     * readonly mode using the EXAMINE command.
     */
    protected boolean hasAccess(Right right) {
        if (session.state != SessionState.SELECTED)
            throw new IllegalStateException();
        switch (right) {
        case SEEN:
        case WRITE:
        case INSERT:
        case DELETE_MESSAGE:
        case EXPUNGE:
            if (session.readonly)
                return false;
        default:
            return accessControl().hasAccess(session.mbname, right);
        }
    }

    public void respondCompletion(Completion c) throws IOException {
        String humanReadableText = c.humanReadableText;
        if (humanReadableText == null) {
            if (c.status.equals("OK")) {
                humanReadableText = parser.command
                        + (parser.subcommand == null ? "" : " " + parser.subcommand) + " completed";
            } else {
                throw new IllegalArgumentException();
            }
        }
        out.respondGenericStatus(tag, c.status, c.code, humanReadableText);
    }

    /**
     * Removes those flags from the supplied set for which the user has no permission in the
     * selected mailbox.
     */
    protected void applyFlagPermissions(MailboxName mbname, MessageFlagSet flags) {
        if (!accessControl().hasAccess(mbname, Right.DELETE_MESSAGE))
            flags.remove(MessageFlag.DELETED.ci());
        if (!accessControl().hasAccess(mbname, Right.SEEN))
            flags.remove(MessageFlag.SEEN.ci());
        if (!accessControl().hasAccess(mbname, Right.WRITE))
            flags.retainAll(Set.of(MessageFlag.DELETED.ci(), MessageFlag.SEEN.ci()));
    }

    /**
     * Returns true if the user have permission to create the mailbox if it does not exist.
     */
    protected boolean canCreateMailbox(MailboxName mbname) throws UnavailableException {
        MailboxName c = mbname.parent();
        while (c != null) {
            if (hierarchy(mbname.namespace).queryNode(c) != null)
                return accessControl().hasAccess(c, Right.CREATE);
            else
                c = c.parent();
        }
        return false;
    }

}
