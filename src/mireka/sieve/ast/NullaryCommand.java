package mireka.sieve.ast;

import mireka.sieve.CallbackException;
import mireka.sieve.Context;
import mireka.sieve.Interpreter.Token;

public class NullaryCommand extends Command {
    public NullaryCommand(Token token) {
        super(token);
    }

    @Override
    public void run(Context context) throws CallbackException {
        trace();
        context.logger.debug("{}", kind);
        switch (kind) {
        case Discard:
            context.implicitKeep = false;
            break;
        case Stop:
            context.stop = true;
            break;
        default:
            throw new AssertionError();
        }
    }

}
