package mireka.sieve.ast;

import mireka.imap.MessageFlagSet;
import mireka.sieve.CallbackException;
import mireka.sieve.Context;
import mireka.sieve.Interpreter.Token;

public class KeepAction extends Command {
    public FlagOption flags = new FlagOption();

    public KeepAction(Token token) {
        super(token);
    }

    @Override
    public void run(Context context) throws CallbackException {
        trace();
        MessageFlagSet actualflags = flags.flags(context);
        context.logger.debug("Keep {}", actualflags);
        context.store(context.mbname.name, actualflags);
        context.implicitKeep = false;
    }

}
