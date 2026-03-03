package mireka.imap.acl;

import java.util.Objects;

public class AccessIdentifier {
    public static final AccessIdentifier ANYONE = new AccessIdentifier("anyone");
    public static final AccessIdentifier AUTHUSER = new AccessIdentifier("authuser");
    public static final AccessIdentifier OWNER = new AccessIdentifier("owner");
    public static final AccessIdentifier ADMINISTRATORS = new AccessIdentifier("administrators");
    public String name;

    private AccessIdentifier(String name) {
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
        AccessIdentifier other = (AccessIdentifier) obj;
        return Objects.equals(name, other.name);
    }

    @Override
    public String toString() {
        return name;
    }

    public static AccessIdentifier from(String name) {
        switch (name) {
        case "anyone":
            return ANYONE;
        case "authuser":
            return AUTHUSER;
        case "owner":
            return OWNER;
        case "administrators":
            return ADMINISTRATORS;
        default:
            return new AccessIdentifier(name);
        }
    }
}
