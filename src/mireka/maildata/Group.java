package mireka.maildata;

import java.util.ArrayList;
import java.util.List;

/**
 * Group represents the group nonterminal, which consists of a group name and a
 * mailbox list.
 */
public class Group extends Address {
    /**
     * It may contain non-ASCII characters.
     */
    public String displayName;
    public List<Mailbox> mailboxList = new ArrayList<>();
}
