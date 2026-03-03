package mireka.sieve.ast;

import mireka.sieve.CallbackException;
import mireka.sieve.Context;
import mireka.sieve.Interpreter.Token;

/**
 * Base class for action and control commands, but not for test commands.
 */
public abstract class Command extends Node {
    public Command(Token token) {
        super(token);
    }

    /**
     * Executes this command.
     * 
     * An implementation must call {@link #trace()} first.
     */
    public abstract void run(Context context) throws CallbackException;
}
