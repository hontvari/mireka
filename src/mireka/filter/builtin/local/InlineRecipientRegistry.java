package mireka.filter.builtin.local;

import java.util.HashSet;
import java.util.Set;

import javax.mail.internet.ParseException;

import mireka.mailaddress.Recipient;
import mireka.mailaddress.MailAddressFactory;
import mireka.mailaddress.RemotePartContainingRecipient;

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
