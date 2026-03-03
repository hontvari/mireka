package mireka.sieve.ast;

import java.util.ArrayList;
import java.util.List;

import mireka.sieve.CallbackException;
import mireka.sieve.Context;

/**
 * A command block, sequential list of commands to be executed
 */
public class Block {
    public List<Command> commands = new ArrayList<>();

    public void run(Context context) throws CallbackException {
        for (Command command : commands) {
            if (context.stop)
                break;
            command.run(context);
        }
    }
}
