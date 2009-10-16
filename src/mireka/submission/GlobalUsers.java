package mireka.submission;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * a simple collection, useful in CDI XML configuration
 */
public class GlobalUsers implements Iterable<GlobalUser> {
    private final Set<GlobalUser> users = new HashSet<GlobalUser>();

    public void addUser(GlobalUser user) {
        if (user == null)
            throw new NullPointerException();
        if (users.contains(user))
            throw new IllegalArgumentException("User " + user.getUsername()
                    + " already included");

        users.add(user);
    }

    @Override
    public Iterator<GlobalUser> iterator() {
        return users.iterator();
    }
}
