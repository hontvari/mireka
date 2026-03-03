package mireka.login;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import mireka.imap.Settings;
import mireka.imap.SettingsRepo;

/**
 * This class authenticates users of a {@link Userlist} collection using different authentication
 * algorithms corresponding to both SMTP and POP3 protocols.
 */
public class UserListLoginSpecification implements LoginSpecification {

    @Inject
    public SettingsRepo settingsRepo;
    private final Map<CiUsername, User> nameUserMap = new HashMap<CiUsername, User>();

    @Override
    public LoginResult evaluatePlain(String usernameString, String password) {
        CiUsername username = new CiUsername(usernameString);
        User user = nameUserMap.get(username);
        if (user == null)
            return new LoginResult(LoginDecision.USERNAME_NOT_EXISTS, null);
        Settings u = settingsRepo.get(user);
        if (u.loginDisabled())
            return new LoginResult(LoginDecision.USERNAME_NOT_EXISTS, null);
        String actualPassword = u.password();
        if (actualPassword.equals(password)) {
            return new LoginResult(LoginDecision.VALID, user);
        } else {
            return new LoginResult(LoginDecision.PASSWORD_DOES_NOT_MATCH, null);
        }
    }

    @Override
    public LoginResult evaluateApop(String usernameString, String timestamp,
            byte[] digestBytes) {
        CiUsername username = new CiUsername(usernameString);
        User user = nameUserMap.get(username);
        if (user == null)
            return new LoginResult(LoginDecision.INVALID, null);
        Settings u = settingsRepo.get(user);
        if (u.loginDisabled())
            return new LoginResult(LoginDecision.INVALID, null);
        String password = u.password();
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
            return new LoginResult(LoginDecision.VALID, user);
        } else {
            return new LoginResult(LoginDecision.INVALID, null);
        }
    }

    @Inject
    public void setUsers(Userlist users) {
        if (!nameUserMap.isEmpty())
            throw new IllegalStateException();

        for (User user : users) {
            Settings u = settingsRepo.get(user);
            nameUserMap.put(new CiUsername(u.name()), u.user());
        }
    }
}
