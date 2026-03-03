package mireka.login;

import java.util.List;

import mireka.filter.local.table.RecipientSpecification;
import mireka.smtp.address.LocalPart;
import mireka.smtp.address.Recipient;

/**
 * A {@link RecipientSpecification} which accepts those recipient addresses where the local part
 * case insensitively matches any of the user names in the supplied user collection. In other words
 * this should be used for any user, who is a valid recipient in all (local) domains in the form of
 * USERNAME@LOCAL_DOMAIN. For example if there is such a user named john, then the recipient
 * addresses john@example.com and john@example.net both will be accepted.
 */
public class GlobalUsersRecipientSpecification implements
        RecipientSpecification {
    private List<UserConfig> users;

    public void setUsers(List<UserConfig> users) {
        this.users = users;
    }

    @Override
    public boolean isSatisfiedBy(Recipient recipient) {
        LocalPart recipientLocalPart = recipient.localPart();
        for (UserConfig user : users) {
            if (user.ciName().matches(recipientLocalPart))
                return true;
        }
        return false;
    }
}
