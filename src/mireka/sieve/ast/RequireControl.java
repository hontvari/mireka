package mireka.sieve.ast;

import java.util.List;

import mireka.sieve.CallbackException;
import mireka.sieve.Context;
import mireka.sieve.Interpreter.Token;

public class RequireControl extends Command {
    public List<String> capabilities;

    public RequireControl(Token token) {
        super(token);
    }

    @Override
    public void run(Context context) throws CallbackException {
        trace();
        // do nothing, the parser manages all extensions at this time
    }

}
