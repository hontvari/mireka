package mireka.login;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple user repository, which can be used to configure users within the configuration files.
 */
public class UserRepo extends Userlist {
    // private List<User> users = new ArrayList<>();
    private Map<User, UserConfig> nameUserMap = new HashMap<>();

    public UserConfig get(User name) {
        return nameUserMap.get(name);
    }

    public void setUsers(List<UserConfig> users) {
        // this.users = users;
        for (UserConfig user : users) {
            nameUserMap.put(user.name(), user);
            names.add(user.name());
        }
    }
}
