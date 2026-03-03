package mireka.sieve.ast;

import mireka.sieve.Context;
import mireka.sieve.Interpreter.Token;

public class NotTc extends Testcommand {
    public Testcommand test;

    public NotTc(Token token, Testcommand test) {
        super(token);
        this.test = test;
    }

    @Override
    public boolean test(Context context) {
        trace();
        return !test.test(context);
    }

}
