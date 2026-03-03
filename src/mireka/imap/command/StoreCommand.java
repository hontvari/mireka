package mireka.imap.command;

import static mireka.imap.Completion.OK;
import static mireka.imap.acl.Right.*;

import java.io.IOException;

import mireka.imap.Completion;
import mireka.imap.ImapException;
import mireka.imap.MessageFlagSet;
import mireka.imap.SequenceSet;
import mireka.imap.Session;
import mireka.imap.UnavailableException;
import mireka.imap.acl.NoPermissionException;
import mireka.imap.parser.CommandParser;
import mireka.imap.parser.CommandParser.Level;
import mireka.imap.parser.CommandSyntaxException;
import mireka.imap.store.Mail;
import mireka.imap.store.MailIterator;
import mireka.imap.store.MailboxName;
import mireka.imap.update.UnilateralResponseOption;

public class StoreCommand extends UidSubcommand {
    private SequenceSet sequences;
    private Mode mode;
    private boolean isSilent;
    private MessageFlagSet flags = new MessageFlagSet();
    /**
     * valid in case of {@link Mode#ADD} and {@link Mode#REPLACE}.
     */
    private MessageFlagSet flagsAdd = new MessageFlagSet();
    /**
     * valid in case of {@link Mode#REMOVE}
     */
    private MessageFlagSet flagsRemove = new MessageFlagSet();

    public StoreCommand(Session session, CommandParser parser) {
        super(session, parser);
    }

    @Override
    public void parse() throws IOException, ImapException {
        parser.take(' ');
        sequences = parser.parseSequenceSet();
        sequences.uid = uidmode;
        parser.take(' ');
        parseAttFlags();
    }

    @Override
    public Completion execute() throws IOException, ImapException {
        MailboxName mbname = session.mbname;
        if (!hasAccess(DELETE_MESSAGE) && !hasAccess(SEEN) && !hasAccess(WRITE))
            throw new NoPermissionException();
        switch (mode) {
        case ADD:
            flagsAdd.addAll(flags);
            applyFlagPermissions(mbname, flagsAdd);
            if (flagsAdd.isEmpty())
                throw new NoPermissionException();
            break;
        case REMOVE:
            flagsRemove.addAll(flags);
            applyFlagPermissions(mbname, flagsRemove);
            if (flagsRemove.isEmpty())
                throw new NoPermissionException();
            break;
        case REPLACE:
            // In case of REPLACE, the flags to be removed change from message to message.
            flagsAdd.addAll(flags);
            applyFlagPermissions(mbname, flagsAdd);
            break;
        }

        try (MailIterator it = mailbox().list(sequences)) {
            Mail mail;
            while ((mail = it.next()) != null) {
                changeFlags(mail);
            }
        }
        return OK;
    }

    /**
     * Parses the data item. Only message flag related data items are defined.
     * 
     * <pre>
     * store-att-flags = (["+" / "-"] "FLAGS" [".SILENT"]) SP (flag-list / (flag *(SP flag)))
     * 
     * <pre>
     */
    private void parseAttFlags() throws CommandSyntaxException, IOException {
        try (Level l = parser.beginLevel("att-flags")) {
            parseDataItemName();
            take(' ');
            // all currently defined data items are followed by a flag list
            if (is('('))
                flags = parser.parseFlagList();
            else
                parseFlags();
        }
    }

    private void parseDataItemName() throws IOException, CommandSyntaxException {
        try (Level l = parser.beginLevel("data-item-name")) {
            if (is('+')) {
                take();
                mode = Mode.ADD;
            } else if (is('-')) {
                take();
                mode = Mode.REMOVE;
            } else {
                mode = Mode.REPLACE;
            }
            parser.takeKeyword("FLAGS");
            if (is('.')) {
                take();
                parser.takeKeyword("SILENT");
                isSilent = true;
            }
        }
    }

    private void parseFlags() throws CommandSyntaxException, IOException {
        try (Level l = parser.beginLevel("att-flags/flag-list/flags")) {
            flags.add(parser.parseFlag());
            while (is(' ')) {
                take();
                flags.add(parser.parseFlag());
            }
        }
    }

    private void changeFlags(Mail mail) throws NoPermissionException, UnavailableException {
        MailboxName mbname = session.mbname;
        MessageFlagSet newFlags = new MessageFlagSet();
        switch (mode) {
        case ADD:
            newFlags.addAll(mail.flags());
            newFlags.addAll(flagsAdd);
            break;
        case REMOVE:
            newFlags.addAll(mail.flags());
            newFlags.removeAll(flagsRemove);
            break;
        case REPLACE:
            // In case of REPLACE, the flags to be removed change from message to message.
            flagsRemove.clear();
            flagsRemove.addAll(mail.flags());
            applyFlagPermissions(mbname, flagsRemove);
            newFlags.addAll(mail.flags());
            newFlags.removeAll(flagsRemove);
            newFlags.addAll(flagsAdd);
            break;
        }
        UnilateralResponseOption option = new UnilateralResponseOption();
        option.notForSession = isSilent ? session : null;
        mail.setFlags(newFlags, option);
    }

    @Override
    public Completion asyncExecute() throws IOException, ImapException {
        throw new IllegalStateException();
    }

    private enum Mode {
        ADD, REMOVE, REPLACE
    };
}
