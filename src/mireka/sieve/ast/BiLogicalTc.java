package mireka.sieve.ast;

import java.util.List;

import mireka.sieve.Context;
import mireka.sieve.Interpreter.Token;
import mireka.sieve.Kind;

public class BiLogicalTc extends Testcommand {
    public List<Testcommand> tests;

    /**
     * {@link Kind#Anyof} or {@link Kind#Allof}
     */
    public BiLogicalTc(Token token, List<Testcommand> tests) {
        super(token);
        this.tests = tests;
    }

    @Override
    public boolean test(Context context) {
        trace();
        switch (kind) {
        case Anyof:
            for (Testcommand t : tests) {
                if (t.test(context))
                    return true;
            }
            return false;
        case Allof:
            for (Testcommand t : tests) {
                if (!t.test(context))
                    return false;
            }
            return true;
        default:
            throw new AssertionError();
        }
    }

}
