package mireka.sieve.ast;

import mireka.sieve.Context;
import mireka.sieve.Interpreter.Token;

public class ConstantTc extends Testcommand {

    public ConstantTc(Token token) {
        super(token);
    }

    @Override
    public boolean test(Context context) {
        trace();
        switch (kind) {
        case False:
            return false;
        case True:
            return true;
        default:
            throw new AssertionError();
        }
    }

}
