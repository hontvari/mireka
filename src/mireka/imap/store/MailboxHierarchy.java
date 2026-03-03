package mireka.imap.store;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import mireka.imap.MailboxFlag;
import mireka.imap.Namespace;
import mireka.imap.NonExistentException;

/**
 * The mailbox hierarchy from the viewpoint of a specific user. It only contains mailboxes for which
 * the user has lookup permission. If he has permission for child node, but not for a parent node,
 * than the parent node will be included but without a mailbox.
 */
public class MailboxHierarchy {
    public final Namespace namespace;
    private final Pattern delimiterPattern;
    /**
     * key is the {@link Node#path}, the full path including the prefix.
     */
    public Map<String, Node> map = new HashMap<>();
    /**
     * The root of the {@link Node} tree, it is a prefix (namespace) node.
     */
    public Node root;

    public MailboxHierarchy(Namespace ns) {
        this.namespace = ns;
        this.delimiterPattern = Pattern.compile(Pattern.quote(ns.delimiter));
        root = new Node();
        root.name = ns.prefix;
        root.path = ns.prefix;
        root.isPrefix = true;
        root.namespace = ns;
        root.parent = null;
    }

    /**
     * Creates the specified node and all missing parent nodes.
     * 
     * @param mbname it must not be a namespace prefix
     * @return the new or existing node corresponding to the mbname.
     */
    public Node createPath(MailboxName mbname) {
        if (!mbname.namespace.equals(namespace))
            throw new IllegalArgumentException();
        Node node = null;
        Node parent = root;
        String pathWithoutPrefix = mbname.path;
        for (String name : delimiterPattern.split(pathWithoutPrefix)) {
            node = parent.children.get(name);
            if (node == null) {
                node = new Node();
                node.name = name;
                if (parent.isPrefix)
                    node.path = parent.path + name;
                else
                    node.path = parent.path + namespace.delimiter + name;
                node.isPrefix = false;
                node.namespace = mbname.namespace;
                node.parent = parent;
                parent.children.put(name, node);
                map.put(node.path, node);
            }
            parent = node;
        }
        if (node == null)
            throw new IllegalArgumentException(
                    "It is assumed that a namespace prefix will not be added, it is created by default");
        return node;
    }

    public Node queryNode(MailboxName name) {
        return map.get(name.original);
    }

    /**
     * Returns null if not exists
     */
    public Mailbox queryMailbox(MailboxName name) {
        Node node = map.get(name.original);
        Mailbox mailbox = null;
        if (node != null)
            mailbox = node.mailbox;
        return mailbox;
    }

    @Deprecated
    public Mailbox mailbox(MailboxName mbname) throws NonExistentException {
        Mailbox mb = queryMailbox(mbname);
        if (mb == null)
            throw new NonExistentException();
        return mb;
    }

    @Deprecated
    public EnumSet<MailboxFlag> mailboxAttributes(MailboxName mbname) {
        EnumSet<MailboxFlag> result = EnumSet.noneOf(MailboxFlag.class);
        Node node = map.get(mbname.original);
        result.addAll(node.flags);
        if (node == null || node.mailbox == null)
            result.add(MailboxFlag.NON_EXISTENT);
        return result;
    }

    public String[] findNames(Pattern pattern) {
        return map.keySet().stream().filter(s -> pattern.matcher(s).matches())
                .toArray(String[]::new);
    }

    public boolean isAnyRecursive(MailboxName mbname, Predicate<Node> predicate) {
        Node n = queryNode(mbname);
        if (n == null)
            return false;
        else
            return n.isAnyRecursive(predicate);
    }
}
