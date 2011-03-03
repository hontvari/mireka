package mireka.pop.command;

import static mireka.pop.SessionState.*;

import java.io.IOException;

import mireka.login.Principal;
import mireka.pop.Command;
import mireka.pop.CommandParser;
import mireka.pop.Pop3Exception;
import mireka.pop.Session;
import mireka.pop.store.Maildrop;
import mireka.pop.store.MaildropLockedException;
import mireka.pop.store.MaildropPopException;

/**
 * Base class for commands which authenticate a user, it provides a function for
 * going into the Transaction state by locking the maildrop associated with the
 * user.
 */
public abstract class AbstractLoginCommand implements Command {

    protected final Session session;

    public abstract void execute(CommandParser commandParser)
            throws IOException, Pop3Exception;

    public AbstractLoginCommand(Session session) {
        this.session = session;
    }

    protected void startTransaction(Principal userPrincipal)
            throws MaildropPopException, IOException {
        String maildropName =
                session.getServer().getPrincipalMaildropTable()
                        .lookupMaildropName(userPrincipal);
        Maildrop maildrop =
                session.getServer().getMaildropRepository()
                        .borrowMaildrop(maildropName);
        try {
            maildrop.beginTransaction();
        } catch (MaildropLockedException e) {
            session.setSessionState(AUTHORIZATION);
            session.getServer().getMaildropRepository()
                    .releaseMaildrop(maildrop);
            session.getThread().sendResponse(e.toResponse());
            return;
        } catch (MaildropPopException e) {
            session.getServer().getMaildropRepository()
                    .releaseMaildrop(maildrop);
            throw e;
        }
        session.setMaildrop(maildrop);
        session.setSessionState(TRANSACTION);
        session.getThread().sendResponse("+OK Maildrop locked and ready");
    }

}