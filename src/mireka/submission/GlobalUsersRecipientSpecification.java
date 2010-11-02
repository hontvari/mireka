package mireka.submission;

import java.util.HashSet;
import java.util.Set;

import mireka.address.LocalPart;
import mireka.address.Recipient;
import mireka.filter.local.table.RecipientSpecification;

/**
 * A {@link RecipientSpecification} which accepts those recipient addresses
 * where the local part case insensitively matches any of the user names in the
 * supplied {@link GlobalUsers} collection.
 */
public class GlobalUsersRecipientSpecification implements
        RecipientSpecification {
    private final Set<LocalPart> usernamesAsLocalParts =
            new HashSet<LocalPart>();

    public void setUsers(GlobalUsers users) {
        if (!usernamesAsLocalParts.isEmpty())
            throw new IllegalStateException();

        for (GlobalUser user : users) {
            usernamesAsLocalParts.add(new LocalPart(user.getUsername()
                    .toString()));
        }
    }

    @Override
    public boolean isSatisfiedBy(Recipient recipient) {
        return usernamesAsLocalParts.contains(recipient.localPart());
    }
}
