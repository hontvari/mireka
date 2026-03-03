package mireka.imap.store;

import java.util.EnumSet;
import java.util.TreeMap;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mireka.imap.MailboxFlag;
import mireka.imap.Namespace;

/**
 * A Node which is part of the mailbox hierarchy as visible from the point of view of the user. This
 * is in contrast to the mailbox hierarchy as stored in the repository.
 */
public class Node implements Comparable<Node> {
    private final Logger logger = LoggerFactory.getLogger(Node.class);

    public Namespace namespace;
    /**
     * Last element of the path, it may be a prefix, which means it can be an empty string.
     */
    public String name;
    /**
     * The full path, including prefix, parent node names separated by '/' and the name of this node
     * which is also separated by '/'.
     */
    public String path;
    /**
     * true if this node represents a prefix.
     */
    public boolean isPrefix;
    public Node parent;
    /**
     * Key is the {@link Node#name} of the child.
     */
    public TreeMap<String, Node> children = new TreeMap<>();
    /**
     * true if the node was explicitly included in the source list of the hierarchy, false if the
     * node is only part of the path to an explicitly included node.
     */
    /**
     * null if no mailbox is associated with this node
     */
    public Mailbox mailbox;
    public boolean isExpicit;
    public EnumSet<MailboxFlag> flags = EnumSet.noneOf(MailboxFlag.class);

    @Override
    public int compareTo(Node o) {
        return name.compareTo(o.name);
    }

    public void debugPrintTree() {
        logger.trace("Node name: {}", name);
        for (Node child : children.values())
            child.debugPrintTree();
    }

    public boolean isAnyRecursive(Predicate<Node> predicate) {
        if (predicate.test(this))
            return true;
        for (Node child : children.values()) {
            if (child.isAnyRecursive(predicate))
                return true;
        }
        return false;
    }
}
