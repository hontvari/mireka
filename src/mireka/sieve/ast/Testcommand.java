package mireka.sieve.ast;

import mireka.sieve.Context;
import mireka.sieve.Interpreter.Token;

public abstract class Testcommand extends Node {
    public Testcommand(Token token) {
        super(token);
    }

    public abstract boolean test(Context context);
}
