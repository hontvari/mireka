package mireka.login;

/**
 * represents a user, who is a valid recipient in all (local) domains in the
 * form of USERNAME@LOCAL_DOMAIN, and who uses a password for login. For example
 * if there is such a user named john, then the recipient addresses
 * john@example.com and john@example.net both will be accepted.
 */
public class GlobalUser {
    private Username username;
    private String password;

    /**
     * @category GETSET
     */
    public Username getUsernameObject() {
        return username;
    }

    /**
     * @category GETSET
     */
    public void setUsername(String username) {
        this.username = new Username(username);
    }

    /**
     * @category GETSET
     */
    public String getPassword() {
        return password;
    }

    /**
     * @category GETSET
     */
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result =
                prime * result + ((username == null) ? 0 : username.hashCode());
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
        GlobalUser other = (GlobalUser) obj;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        return true;
    }
}
