package mireka.sieve.ast;

import static mireka.sieve.Kind.Is;

import java.util.List;

import org.hamcrest.core.Is;

import mireka.sieve.Comparator;
import mireka.sieve.Comparator.CompiledKey;
import mireka.sieve.Comparator.CompiledValue;
import mireka.sieve.Kind;

public class MatchType {
    public boolean specified;
    /**
     * Valid values: {@link Is}, {@link Kind#Contains}, {@link Kind#Matches}.
     */
    public Kind value = Is;

    protected boolean compare(Comparator comp, List<String> keys, List<String> values) {
        CompiledKey[] ckeys = keys.stream().map(k -> comp.compileKey(k, value))
                .toArray(CompiledKey[]::new);
        CompiledValue[] cvalues = values.stream().map(v -> comp.compileValue(v, value))
                .toArray(CompiledValue[]::new);
        for (CompiledKey ckey : ckeys) {
            for (CompiledValue cvalue : cvalues) {
                switch (value) {
                case Is:
                    if (comp.is(ckey, cvalue))
                        return true;
                    break;
                case Contains:
                    if (comp.contains(ckey, cvalue))
                        return true;
                    break;
                case Matches:
                    if (comp.match(ckey, cvalue))
                        return true;
                    break;
                default:
                    throw new AssertionError();
                }
            }
        }
        return false;
    }

}