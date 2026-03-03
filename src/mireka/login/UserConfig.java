package mireka.login;

import mireka.imap.Settings;

/**
 * Stores a user's login and configuration data, like the user name and the password he uses for
 * login.
 * 
 * If possible use a Username object to pass around a handle to the user, instead of this object.
 * Instead of directly accessing the settings like fields of this class, use {@link Settings}.
 */
public class UserConfig {
    public User name;
    private CiUsername ciName;
    public String password;
    public boolean loginDisabled;
    public boolean imapHasPersonalNamespace;
    /**
     * The internal, maybe not-routable domain which is mostly used by the user. It must be in the
     * format specified by RFC 5322 for the domain nonterminal.
     * 
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc5322#section-3.4.1">RFC 5322 Addr-Spec
     * Specification</a>
     */
    public String internalDomain;
    public String internalName;
    public String internalAddress;
    public String postmasterName;
    public String postmasterAddress;
    public String sieveScript;
    /**
     * BCP 47 tag, for example "de", "en-US"
     */
    public String locale;

    public User name() {
        return name;
    }

    /**
     * Currently this class always returns a local independent case-insensitive user name, it is not
     * possible to configure case sensitivity.
     */
    public CiUsername ciName() {
        return ciName;
    }

    public void setUsername(String name) {
        this.name = new User(name);
        this.ciName = new CiUsername(name);
    }
}
