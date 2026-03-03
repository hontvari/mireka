package mireka.imap;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

import mireka.ConfigurationException;
import mireka.login.CiUsername;
import mireka.login.User;
import mireka.maildata.type.AddrSpec;
import mireka.maildata.type.DomainPart;
import mireka.maildata.type.Mailbox;

/**
 * User settings. These functions never return null. if some option is not configured they must
 * return a default value, maybe constructed from other data. If that is not possible it must throw
 * a {@link ConfigurationException}.
 */
public interface Settings {
    User user();
    /**
     * Canonical username
     */
    String name();

    /**
     * A user name, which can be case insensitive or not, depending on the configuration, it can be
     * used to match local part of email addresses or login names received during authentication.
     */
    CiUsername ciName();

    String password();

    boolean loginDisabled();

    boolean hasPersonalNamespace();

    /**
     * @return 0 means unlimited
     */
    long getQuota();

    /**
     * The internal, maybe not-routable domain which is mostly used by the user.
     */
    DomainPart internalDomain();

    /**
     * A familiar name of the user, usually the same as the username, maybe capitalized, includes
     * unicode characters. It can appear in the TO address field of system generated emails.
     */
    String internalName();
    /**
     * The remote part may be a non-routable domain. It may appear in the FROM field of system
     * generated emails and in the recipients part of the mail envelope.
     */
    AddrSpec internalAddress();

    /**
     * {@link #internalName()} plus {@link #internalAddress()}
     */
    default Mailbox internalMailbox() {
        Mailbox m = new Mailbox();
        m.displayName = internalName();
        m.addrSpec = internalAddress();
        return m;
    }

    /**
     * Display name of the postmaster associated with the user
     */
    String postmasterName();

    /**
     * Email address of the postmaster associated with the user
     */
    AddrSpec postmasterAddress();

    default Mailbox postmasterMailbox() {
        Mailbox m = new Mailbox();
        m.displayName = postmasterName();
        m.addrSpec = postmasterAddress();
        return m;
    }

    /**
     * The file path to the sieve script of the user.
     */
    Optional<Path> sieveScript();

    /**
     * In which locale notifications should be sent to this suer
     */
    Locale locale();
}
