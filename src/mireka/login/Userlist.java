package mireka.login;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Userlist implements Iterable<User> {
    public List<User> names = new ArrayList<User>();

    @Override
    public Iterator<User> iterator() {
        return names.iterator();
    }
}
