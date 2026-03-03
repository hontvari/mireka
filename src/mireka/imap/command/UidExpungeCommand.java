package mireka.imap.command;

import static mireka.imap.Completion.OK;
import static mireka.imap.acl.Right.EXPUNGE;

import java.io.IOException;

import mireka.imap.Completion;
import mireka.imap.ImapException;
import mireka.imap.SequenceSet;
import mireka.imap.Session;
import mireka.imap.acl.NoPermissionException;
import mireka.imap.parser.CommandParser;
import mireka.imap.update.UnilateralResponseOption;

public class UidExpungeCommand extends UidSubcommand {
    private SequenceSet sequences;

    public UidExpungeCommand(Session session, CommandParser parser) {
        super(session, parser);
    }

    @Override
    public void parse() throws IOException, ImapException {
        take(' ');
        sequences = parser.parseSequenceSet();
        sequences.uid = true;
    }

    @Override
    public Completion execute() throws IOException, ImapException {
        if (!hasAccess(EXPUNGE))
            throw new NoPermissionException();
        UnilateralResponseOption option = new UnilateralResponseOption();
        session.mailbox.expunge(sequences, option);
        return OK;
    }

    @Override
    public Completion asyncExecute() throws IOException, ImapException {
        throw new IllegalStateException();
    }

}
