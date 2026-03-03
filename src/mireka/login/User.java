package mireka.login;

/**
 * This class represents the canonical name of a user account. If possible, use this object instead
 * of {@link UserConfig}.
 * 
 * Idea: this should be renamed to simply User.
 */
public class User {
    private final String name;

    public User(String name) {
        super();
        this.name = name;
    }

    public String name() {
        return name;
    }

    public boolean isAnonymous() {
        return name.equals("anonymous");
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return name;
    }

}
