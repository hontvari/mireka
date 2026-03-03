package mireka.sieve.ast;

import mireka.imap.MessageFlagSet;
import mireka.sieve.CallbackException;
import mireka.sieve.Context;
import mireka.sieve.Interpreter.Token;

public class FileintoAction extends Command {
    public FlagOption flags = new FlagOption();
    public String mailbox;

    public FileintoAction(Token token) {
        super(token);
    }

    @Override
    public void run(Context context) throws CallbackException {
        trace();
        MessageFlagSet actualflags = flags.flags(context);
        context.logger.debug("Fileinto \"{}\" {}", mailbox, actualflags);
        context.store(mailbox, actualflags);
        context.implicitKeep = false;
    }
}
