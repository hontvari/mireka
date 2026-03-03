package mireka.sieve.ast;

import static mireka.sieve.ComparatorKind.ASCII_CASEMAP;

import mireka.sieve.Comparator;
import mireka.sieve.ComparatorKind;

public class ComparatorType {
    public boolean specified;
    public ComparatorKind value = ASCII_CASEMAP;

    public Comparator get() {
        return new Comparator.RegexpComparator(value);
    }
}