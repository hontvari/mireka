package mireka.submission;

import java.util.HashMap;
import java.util.Map;

public class GlobalUsersLoginSpecification implements LoginSpecification {

    private final Map<Username, String> usernamePasswordMap =
            new HashMap<Username, String>();

    public void setUsers(GlobalUsers users) {
        if (!usernamePasswordMap.isEmpty())
            throw new IllegalStateException();

        for (GlobalUser user : users) {
            usernamePasswordMap.put(user.getUsername(), user.getPassword());
        }
    }

    @Override
    public LoginResult evaluate(String usernameString,
            String password) {
        Username username = new Username(usernameString);
        String actualPassword = usernamePasswordMap.get(username);
        if (actualPassword == null) {
            return LoginResult.USERNAME_NOT_EXISTS;
        } else if (actualPassword.equals(password)) {
            return LoginResult.VALID;
        } else {
            return LoginResult.PASSWORD_DOES_NOT_MATCH;
        }
    }
}
