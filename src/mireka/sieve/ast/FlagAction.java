package mireka.sieve.ast;

import java.util.List;

import mireka.imap.MessageFlagSet;
import mireka.sieve.CallbackException;
import mireka.sieve.Context;
import mireka.sieve.Interpreter.Token;
import mireka.sieve.Kind;

public class FlagAction extends Command {
    public List<String> flagNames;
    /**
     * Possible values: {@link Kind#Setflag}, {@link Kind#Addflag}, {@link Kind#Removeflag}
     */
    public FlagAction(Token token) {
        super(token);
    }

    @Override
    public void run(Context context) throws CallbackException {
        trace();
        MessageFlagSet flags = FlagOption.parseFlagNames(flagNames);
        switch (kind) {
        case Setflag:
            context.messageFlags = flags;
            break;
        case Addflag:
            context.messageFlags.addAll(flags);
            break;
        case Removeflag:
            context.messageFlags.retainAll(flags);
            break;
        default:
            throw new AssertionError();
        }
    }
}
