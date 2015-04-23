package mireka.maildata;

import java.util.ArrayList;
import java.util.List;

/**
 * Group represents the group nonterminal, which consists of a group name and a
 * mailbox list.
 */
public class Group extends Address {
    public String displayName;
    public List<Mailbox> mailboxList = new ArrayList<>();
}
