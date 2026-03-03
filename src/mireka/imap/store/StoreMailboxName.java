package mireka.imap.store;

import java.util.Objects;

/**
 * It can be either the name of an actual mailbox as is it is stored (including the namespace
 * prefix), or a currently not existing mailbox name for the subscribe command, or a standalone
 * store namespace prefix, useful to assign ACL. The name must not end in a separator. However if a
 * prefix ends to the separator character that is valid. Store namespace prefixes: "#shared/", "~",
 * "~username/".
 */
public class StoreMailboxName {
    public final String name;

    public StoreMailboxName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StoreMailboxName other = (StoreMailboxName) obj;
        return Objects.equals(name, other.name);
    }

    @Override
    public String toString() {
        return name;
    }

}
