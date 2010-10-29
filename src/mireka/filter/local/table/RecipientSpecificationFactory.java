package mireka.filter.local.table;

import java.util.Locale;

import mireka.address.MailAddressFactory;
import mireka.address.RemotePart;

/**
 * RecipientSpecificationFactory can convert a mail address like string into a
 * {@link RecipientSpecification} instance.
 */
public class RecipientSpecificationFactory {
    private static final String POSTMASTER_AT = "postmaster@";
    private final MailAddressFactory mailAddressFactory;

    public RecipientSpecificationFactory() {
        this(new MailAddressFactory());
    }

    /**
     * This constructor is useful for unit testing.
     */
    RecipientSpecificationFactory(MailAddressFactory mailAddressFactory) {
        this.mailAddressFactory = mailAddressFactory;
    }

    /**
     * Given a mailbox address supplied as a string, it creates a corresponding
     * {@link RecipientSpecification}, either a specification which requires a
     * specific case insensitive local part combined with a specific remote
     * part, or a {@link GlobalPostmasterSpecification} or a
     * {@link DomainPostmasterSpecification}.
     * 
     * @throws IllegalArgumentException
     *             if the syntax of the supplied mailbox is invalid.
     */
    public RecipientSpecification create(String mailbox)
            throws IllegalArgumentException {
        String mailboxLowerCase = mailbox.toLowerCase(Locale.US);
        if ("postmaster".equals(mailboxLowerCase)) {
            return new GlobalPostmasterSpecification();
        } else if (mailboxLowerCase.startsWith(POSTMASTER_AT)) {
            DomainPostmasterSpecification domainPostmaster =
                    new DomainPostmasterSpecification();
            String remotePartString = mailbox.substring(POSTMASTER_AT.length());
            domainPostmaster.setRemotePart(mailAddressFactory
                    .createRemotePart(remotePartString));
            return domainPostmaster;
        } else {
            int iAt = mailbox.indexOf('@');
            if (iAt == -1 || iAt == 0 || iAt == mailbox.length() - 1)
                throw new IllegalArgumentException("Recipient specification "
                        + mailbox + " must contain a '@' symbol "
                        + "and both local and remote parts, "
                        + "except for the global postmaster mailbox");
            String localPartString = mailbox.substring(0, iAt);
            LocalPartSpecification localPart =
                    new CaseInsensitiveLocalPartSpecification(localPartString);
            String remotePartString = mailbox.substring(iAt + 1);
            RemotePart remotePart =
                    mailAddressFactory.createRemotePart(remotePartString);
            return new LocalRemoteCombinedRecipientSpecification(localPart,
                    remotePart);
        }
    }
}
