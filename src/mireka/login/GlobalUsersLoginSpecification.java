package mireka.login;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class authenticates users of a {@link GlobalUsers} collection using
 * different authentication algorithms corresponding to both SMTP and POP3
 * protocols.
 */
public class GlobalUsersLoginSpecification implements LoginSpecification {

    private final Map<Username, String> usernamePasswordMap =
            new HashMap<Username, String>();
    private final Map<Username, Principal> usernamePrincipalMap =
            new HashMap<Username, Principal>();

    public void setUsers(GlobalUsers users) {
        if (!usernamePasswordMap.isEmpty())
            throw new IllegalStateException();

        for (GlobalUser user : users) {
            usernamePasswordMap.put(user.getUsername(), user.getPassword());
            usernamePrincipalMap.put(user.getUsername(), new Principal(user
                    .getUsername().toString()));
        }
    }

    @Override
    public LoginResult evaluatePlain(String usernameString, String password) {
        Username username = new Username(usernameString);
        String actualPassword = usernamePasswordMap.get(username);
        if (actualPassword == null) {
            return new LoginResult(LoginDecision.USERNAME_NOT_EXISTS, null);
        } else if (actualPassword.equals(password)) {
            return new LoginResult(LoginDecision.VALID,
                    usernamePrincipalMap.get(username));
        } else {
            return new LoginResult(LoginDecision.PASSWORD_DOES_NOT_MATCH, null);
        }
    }

    @Override
    public LoginResult evaluateApop(String usernameString, String timestamp,
            byte[] digestBytes) {
        Username username = new Username(usernameString);
        String password = usernamePasswordMap.get(username);
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Assertion failed");
        }
        String text = timestamp + password;
        byte[] textBytes;
        try {
            textBytes = text.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Assertion failed");
        }
        byte[] calculatedDigestBytes = digest.digest(textBytes);
        boolean isValid =
                MessageDigest.isEqual(digestBytes, calculatedDigestBytes);
        if (isValid) {
            return new LoginResult(LoginDecision.VALID,
                    usernamePrincipalMap.get(username));
        } else {
            return new LoginResult(LoginDecision.INVALID, null);
        }
    }
}
