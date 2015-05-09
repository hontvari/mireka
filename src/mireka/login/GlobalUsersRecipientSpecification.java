package mireka.login;

import mireka.filter.local.table.RecipientSpecification;
import mireka.smtp.address.LocalPart;
import mireka.smtp.address.Recipient;

/**
 * A {@link RecipientSpecification} which accepts those recipient addresses
 * where the local part case insensitively matches any of the user names in the
 * supplied {@link GlobalUsers} collection.
 */
public class GlobalUsersRecipientSpecification implements
        RecipientSpecification {
    private GlobalUsers users;

    public void setUsers(GlobalUsers users) {
        this.users = users;
    }

    @Override
    public boolean isSatisfiedBy(Recipient recipient) {
        LocalPart recipientLocalPart = recipient.localPart();
        for (GlobalUser user : users) {
            if (user.getUsernameObject().matches(recipientLocalPart))
                return true;
        }
        return false;
    }
}
