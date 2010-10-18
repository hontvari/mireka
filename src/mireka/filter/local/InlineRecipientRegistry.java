package mireka.filter.local;

import java.util.HashSet;
import java.util.Set;

import javax.mail.internet.ParseException;

import mireka.address.MailAddressFactory;
import mireka.address.Recipient;
import mireka.address.RemotePartContainingRecipient;

public class InlineRecipientRegistry implements RecipientSpecification {
    private final Set<Recipient> recipients = new HashSet<Recipient>();

    public void addAddress(String address) {
        try {
            Recipient recipient =
                    new MailAddressFactory().createRecipient(address);
            recipients.add(recipient);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isSatisfiedBy(RemotePartContainingRecipient recipient) {
        return recipients.contains(recipient);
    }
}
