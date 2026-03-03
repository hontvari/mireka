package mireka.imap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NamespaceCatalogue {
    public final List<Namespace> personals = new ArrayList<>();
    public final List<Namespace> others = new ArrayList<>();
    public final List<Namespace> shareds = new ArrayList<>();
    /**
     * List of namespaces sorted by their prefix length. First is the namespace with the longest
     * prefix.
     */
    private final List<Namespace> prefixes = new ArrayList<>();

    /**
     * Returns the namespace which corresponds to the prefix of the supplied mailbox name or mailbox
     * search expression.
     * 
     * @param mailbox a mailbox or search expression from the viewpoint of the user. For example
     * INBOX or #shared/sysadm.
     */
    public Namespace forName(String mailbox) {
        for (Namespace ns : prefixes) {
            if (mailbox.startsWith(ns.prefix))
                return ns;
        }
        throw new RuntimeException("This should not happen");
    }

    public void add(Namespace ns) {
        switch (ns.kind) {
        case PERSONAL:
            personals.add(ns);
            break;
        case OTHER_USERS:
            others.add(ns);
            break;
        case SHARED:
            shareds.add(ns);
            break;
        }
        prefixes.add(ns);
        prefixes.sort(new Comparator<Namespace>() {
            @Override
            public int compare(Namespace o1, Namespace o2) {
                return -(o1.prefix.length() - o2.prefix.length());
            }
        });
    }
}

