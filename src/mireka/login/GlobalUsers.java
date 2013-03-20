package mireka.login;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * GlobalUsers is a simple collection of GlobalUser instances, this class is
 * useful in CDI XML configuration.
 */
public class GlobalUsers implements Iterable<GlobalUser> {
    private final Set<GlobalUser> users = new HashSet<GlobalUser>();

    @Override
    public Iterator<GlobalUser> iterator() {
        return users.iterator();
    }

    public void addUser(GlobalUser user) {
        if (user == null)
            throw new NullPointerException();
        if (users.contains(user))
            throw new IllegalArgumentException("User "
                    + user.getUsernameObject() + " already included");

        users.add(user);
    }

    public void setUsers(List<GlobalUser> users) {
        this.users.clear();

        for (GlobalUser user : users)
            addUser(user);
    }
}
