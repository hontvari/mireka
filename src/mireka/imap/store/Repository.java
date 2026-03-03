package mireka.imap.store;

import static mireka.imap.NamespaceKind.*;

import java.util.Locale;

import javax.annotation.Nullable;

import mireka.imap.CompletionException;
import mireka.imap.Namespace;
import mireka.imap.NamespaceCatalogue;
import mireka.imap.NonExistentException;
import mireka.imap.UnavailableException;
import mireka.imap.server.ServerThread;
import mireka.imap.update.UnilateralResponseOption;
import mireka.imap.update.Update;
import mireka.login.User;

/**
 * Repository represents a collection of mailboxes. One user has many mailboxes and there may be
 * mailboxes which are shared by users and are not related to one particular user. A user may have
 * given access to mailboxes of other users. Only one Repository is assigned to an IMAP user.
 */
public interface Repository {

    /**
     * Registers a {@link StoreTransaction} into the {@link Transaction} instance.
     */
    void begin();

    default NamespaceCatalogue namespaces() {
        NamespaceCatalogue nsc = new NamespaceCatalogue();
        Namespace ns = new Namespace();
        ns.kind = PERSONAL;
        ns.prefix = "";
        ns.delimiter = "/";
        nsc.add(ns);
        ns = new Namespace();
        ns.kind = OTHER_USERS;
        ns.prefix = "~";
        ns.delimiter = "/";
        nsc.add(ns);
        ns = new Namespace();
        ns.kind = SHARED;
        ns.prefix = "#shared/";
        ns.delimiter = "/";
        nsc.add(ns);
        return nsc;
    }

    /**
     * Returns null if the specified mailbox does not exist.
     */
    @Nullable
    Mailbox queryMailbox(MailboxName name) throws UnavailableException;

    void createDefaultMailboxes(User user) throws UnavailableException;

    void createMailbox(MailboxName name) throws AlreadyExistsException, CompletionException;

    /**
     * Removes the mailbox from the catalog, but it does not actually delete it.
     */
    Mailbox removeMailbox(MailboxName mbname, UnilateralResponseOption option)
            throws NonExistentException, CompletionException;

    void subscribe(MailboxName mbname) throws UnavailableException;

    /**
     * Returns a mailbox hierarchy with names which are defined from the viewpoint of the user. For
     * example from the viewpoint of the user there is a single mailbox which is named INBOX. In
     * contrast, from the viewpoint of the repository these INBOX mailboxes each have a unique name,
     * which is used within the store. The root node is the prefix, the root of the namespace. It is
     * possible that the hierarcy is empty, only the root node is present.
     * 
     * Obsolete: The returned node is the root of the hierarchy, the root node is only a collection,
     * not a valid name. The second level nodes are namespaces. A namespace may have an empty
     * prefix. In that case the namespace node is not a part of the path, similarly to the root
     * node.
     */
    MailboxHierarchy userMailboxHierarchy(User user, Namespace ns);

    MailboxHierarchy subsribedMailboxHierarchy(User user, Namespace ns);

    /**
     * Returns a parsed form of the supplied mailbox name.
     * 
     * @param m the mailbox name from the viewpoint of the user
     */
    default MailboxName parseMailboxName(User user, String m) {
        String storeName;
        MailboxName pm = new MailboxName();
        pm.user = user;
        pm.original = m;
        pm.namespace = namespaces().forName(m);
        int level = (int) m.codePoints().filter(c -> c == '/').count() + 1;
        if (m.endsWith("/"))
            level--;
        else if (m.isEmpty())
            level = 0;
        if (m.toUpperCase(Locale.US).equals("INBOX")) {
            pm.owner = user;
            pm.path = "INBOX";
            pm.slashEnding = false;
            pm.inbox = true;
            storeName = "~" + user.name() + "/INBOX";
            pm.level = 1;
        } else if (m.startsWith("#shared/")) {
            pm.owner = null;
            pm.path = m.substring("#shared/".length());
            storeName = m;
            pm.level = level;
        } else if (m.startsWith("~")) {
            int firstSlash = m.indexOf('/');
            pm.owner = new User(m.substring(1, firstSlash));
            pm.path = m.substring("~".length());
            storeName = m;
            pm.level = level;
            if (pm.owner.equals(user)) {
                // this is wrong, use a dummy
                storeName = null;
                pm.ownInOtherUsersNamespace = true;
            }
        } else {
            pm.owner = user;
            pm.path = m;
            storeName = "~" + user.name() + "/" + m;
        }
        if (pm.path.endsWith("/")) {
            pm.path = pm.path.substring(0, pm.path.length() - 1);
            pm.slashEnding = true;
        } else {
            pm.slashEnding = false;
        }
        pm.name = pm.namespace.prefix + pm.path;
        if (storeName != null) {
            if (storeName.endsWith("/")) {
                storeName = storeName.substring(0, storeName.length() - 1);
            }
            pm.storeName = new StoreMailboxName(storeName);
        } else {
            pm.storeName = null;

        }
        return pm;
    }

    MailboxName parseStoreMailboxName(User user, StoreMailboxName sname);

    /**
     * Registers a {@link ServerThread} which will receive {@link Update}s about new and removed
     * mailboxes.
     */
    void addServerThread(ServerThread serverThread);

}
