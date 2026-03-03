package mireka.imap.store;

import java.util.Locale;
import java.util.Objects;

import mireka.imap.Namespace;
import mireka.imap.NamespaceKind;
import mireka.login.User;

/**
 * A parsed mailbox name, mostly from the viewpoint of the user and IMAP client, and not from the
 * storage backend. For example the {@link H2Repository} backend stores mailboxes like ~john/INBOX,
 * while the IMAP client simply refers to that as INBOX (or even inbox), assuming that john is the
 * authenticated user.
 */
public class MailboxName implements Comparable<MailboxName> {
    /**
     * the full name, from the viewpoint of the user, without ending separator.
     */
    public String name;
    /**
     * namespace for example #news. It may be an empty string. It is not obvious where is this used.
     */
    public Namespace namespace;
    /**
     * the user who owns the personal namespace in which the supplied mailbox exists
     */
    public User owner;
    /**
     * other informations are from the perspective of this user
     */
    public User user;
    /**
     * after the prefix, without a closing separator.
     */
    public String path;
    /**
     * true if the original mailbox string is ended with a separator character.
     */
    public boolean slashEnding;
    /**
     * true means the mailbox name is "INBOX", i.e. it refers to the default personal mailbox of
     * the current user.
     */
    public boolean inbox;
    /**
     * the name refers to a mailbox of the authenticated user but within the other users namespace.
     * This form mustn't be used, because it results in duplicated mailboxes on the user interface.
     */
    public boolean ownInOtherUsersNamespace;
    /**
     * the supplied original string without modification, maybe with an ending separator
     */
    public String original;
    /**
     * The name as stored in the database. Null if invalid, that is if
     * {@link #ownInOtherUsersNamespace} is true.
     */
    public StoreMailboxName storeName;
    /**
     * count of hierarchy levels, empty string is 0
     */
    public int level;

    public MailboxName() {

    }

    /**
     * copy constructor
     */
    public MailboxName(MailboxName o) {
        this.name = o.name;
        this.namespace = o.namespace;
        this.owner = o.owner;
        this.user = o.user;
        this.path = o.path;
        this.slashEnding = o.slashEnding;
        this.inbox = o.inbox;
        this.ownInOtherUsersNamespace = o.ownInOtherUsersNamespace;
        this.original = o.original;
        this.storeName = o.storeName;
        this.level = o.level;
    }

    /**
     * Returns the parent mailbox, from the viewpoint of the user.
     * 
     * @return null, if there is no parent. "" empty string means the root element within the
     * namespace.
     */
    public MailboxName parent() {
        MailboxName n = new MailboxName(this);
        if (path.isEmpty())
            return null;
        n.original = null;
        int i = path.lastIndexOf(namespace.delimiter);
        if (i == -1) {
            n.path = "";
            n.level = 0;
            n.inbox = false;
        } else {
            n.path = n.path.substring(i);
            n.level--;
            n.inbox = namespace.kind == NamespaceKind.PERSONAL
                    && "INBOX".equals(n.path.toUpperCase(Locale.US));
        }
        switch (n.namespace.kind) {
        case PERSONAL:
            n.storeName = new StoreMailboxName(
                    "~" + owner.name() + namespace.delimiter + n.path);
            break;
        case OTHER_USERS:
        case SHARED:
            n.storeName = new StoreMailboxName(n.namespace.prefix + n.path);
            break;
        default:
            throw new RuntimeException();
        }
        return n;
    }

    @Override
    public int compareTo(MailboxName o) {
        if (inbox && o.inbox)
            return 0;
        else if (inbox && !o.inbox)
            return -1;
        else if (!inbox && o.inbox)
            return 1;
        else
            return name.compareTo(o.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, user);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MailboxName other = (MailboxName) obj;
        return Objects.equals(name, other.name) && Objects.equals(user, other.user);
    }

    @Override
    public String toString() {
        return "MailboxName [original=" + original + ", storeName=" + storeName + "]";
    }
}