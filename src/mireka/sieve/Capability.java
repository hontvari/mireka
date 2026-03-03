package mireka.sieve;

import java.util.Optional;

public enum Capability {
    Envelope("envelope"), Fileinto("fileinto"), Imap4flags("imap4flags");

    /**
     * case sensitive capability string
     */
    public String name;

    private Capability(String name) {
        this.name = name;
    }

    public static Optional<Capability> forName(String name) {
        for (Capability c : values()) {
            if (c.name.equals(name))
                return Optional.of(c);
        }
        return Optional.empty();
    }
}
