package mireka.imap;

import java.util.Objects;

import javax.annotation.Nullable;

public class Namespace {
    public NamespaceKind kind;
    /**
     * namespace prefix, for example '#news.'. It may be an empty string.
     */
    public String prefix;
    public @Nullable String delimiter;

    @Override
    public int hashCode() {
        return Objects.hash(prefix);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Namespace other = (Namespace) obj;
        return Objects.equals(prefix, other.prefix);
    }
}