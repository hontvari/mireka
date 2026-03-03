package mireka.sieve.ast;

import mireka.sieve.CallbackException;
import mireka.sieve.Context;

public class Program extends Block {

    @Override
    public void run(Context context) throws CallbackException {
        super.run(context);
        if (context.implicitKeep) {
            context.logger.debug("Keep (implicit)");
            context.store(context.mbname.name, context.messageFlags);
        }
    }

}
