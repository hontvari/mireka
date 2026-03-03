package mireka.sieve.ast;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import mireka.sieve.CallbackException;
import mireka.sieve.Context;
import mireka.sieve.Interpreter.Token;

public class IfControl extends Command {
    public List<Entry> conditionalBlocks = new ArrayList<>();
    @Nullable
    public Block elseBlock;

    public IfControl(Token token) {
        super(token);
    }

    public void add(Testcommand test, Block block) {
        Entry entry = new Entry();
        entry.test = test;
        entry.block = block;
        conditionalBlocks.add(entry);
    }

    @Override
    public void run(Context context) throws CallbackException {
        trace();
        for (Entry entry : conditionalBlocks) {
            if (entry.test.test(context)) {
                entry.block.run(context);
                return;
            }
        }
        if (elseBlock != null)
            elseBlock.run(context);
    }

    public static class Entry {
        public Testcommand test;
        public Block block;
    }
}
