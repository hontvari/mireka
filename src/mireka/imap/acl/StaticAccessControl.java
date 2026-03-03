package mireka.imap.acl;

import static mireka.imap.NamespaceKind.*;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mireka.imap.store.MailboxName;
import mireka.imap.store.StoreMailboxName;
import mireka.login.User;

public class StaticAccessControl implements AccessControl {
    private static EnumSet<Right> RIGHTS_FOR_SHARED = EnumSet.of(Right.LOOKUP, Right.READ,
            Right.SEEN);
    private static Set<AccessIdentifier> EMPTY = Collections.emptySet();
    private static ACL EMPTY_ACL = new ACL();
    private final Logger logger = LoggerFactory.getLogger(StaticAccessControl.class);
    private boolean isOwnerHasDefaultRights;
    private boolean isSharedMailboxesAreReadableByAnyone;

    private final Map<User, Set<AccessIdentifier>> membership = new HashMap<>();
    private final Map<StoreMailboxName, ACL> acls = new HashMap<>();

    @Override
    public boolean hasAccess(MailboxName mbname, Right operation) {
        User user = mbname.user;
        if (isAdmin(user))
            return true;
        if (mbname.ownInOtherUsersNamespace)
            return false; // this indicates an illegal name
        if (isOwnerHasDefaultRights && mbname.namespace.kind == PERSONAL
                && user.equals(mbname.owner))
            return true;
        if (isSharedMailboxesAreReadableByAnyone && mbname.namespace.kind == SHARED
                && RIGHTS_FOR_SHARED.contains(operation))
            return true;
        return rights(mbname).contains(operation);
    }

    /**
     * Returns the actually stored rights, not including extra rights for administrators and default
     * rights for owners.
     */
    private EnumSet<Right> rights(MailboxName mbname) {
        ACL acl = acls.getOrDefault(mbname, EMPTY_ACL);
        return acl.rights(identifiers(mbname));
    }

    private boolean isAdmin(User user) {
        return membership.getOrDefault(user, EMPTY).contains(AccessIdentifier.ADMINISTRATORS);
    }

    /**
     * Returns the set of {@link AccessIdentifier}s assigned to the current user, including virtual
     * groups and the user itself.
     * 
     * @param mbname it identifies both the mailbox and the current user
     */
    private Set<AccessIdentifier> identifiers(MailboxName mbname) {
        User user = mbname.user;
        Set<AccessIdentifier> identifiers = new HashSet<AccessIdentifier>();
        identifiers.addAll(membership.getOrDefault(user, EMPTY));
        identifiers.add(AccessIdentifier.ANYONE);
        if (!user.isAnonymous())
            identifiers.add(AccessIdentifier.AUTHUSER);
        if (user.equals(mbname.owner))
            identifiers.add(AccessIdentifier.OWNER);
        identifiers.add(AccessIdentifier.from(user.name()));
        return identifiers;
    }

    /**
     * Used for configuration
     * 
     * @param name group name
     * @param members the name of users who are members of the group
     */
    public void addGroup(String name, String[] members) {
        if (logger.isTraceEnabled())
            logger.trace("group {}: {}", name, String.join(", ", members));
        AccessIdentifier identifier = AccessIdentifier.from(name);
        for (String member : members) {
            User user = new User(member);
            membership.computeIfAbsent(user, x -> new HashSet<AccessIdentifier>()).add(identifier);
        }
    }

    /**
     * Used for configuration
     */
    public void addAcl(String mailbox, ACL acl) {
        logger.trace("acl for {}: {}", mailbox, acl);
        acls.put(new StoreMailboxName(mailbox), acl);
    }

    public void setIsOwnerHasDefaultPermissions(boolean isOwnerHasDefaultPermissions) {
        this.isOwnerHasDefaultRights = isOwnerHasDefaultPermissions;
    }

    public void setIsSharedMailboxesAreReadableByAnyone(
            boolean isSharedMailboxesAreReadableByAnyone) {
        this.isSharedMailboxesAreReadableByAnyone = isSharedMailboxesAreReadableByAnyone;
    }

}
