package mireka.sieve;

import java.util.Optional;

public enum ComparatorKind {
    OCTET("i;octet"), ASCII_CASEMAP("i;ascii-casemap"), UNICODE_CASEMAP("i;unicode-casemap");

    public String id;

    private ComparatorKind(String id) {
        this.id = id;
    }

    public static Optional<ComparatorKind> forId(String id) {
        for (ComparatorKind k : values()) {
            if (k.id.equals(id))
                return Optional.of(k);
        }
        return Optional.empty();
    }
}