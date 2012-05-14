package mireka.filter.local.table;

import java.text.ParseException;

import mireka.address.MailAddressFactory;
import mireka.address.RemotePart;
import mireka.address.parser.RecipientParser;
import mireka.address.parser.ast.DomainPostmasterRecipientAST;
import mireka.address.parser.ast.MailboxRecipientAST;
import mireka.address.parser.ast.RecipientAST;
import mireka.address.parser.ast.SystemPostmasterRecipientAST;

/**
 * RecipientSpecificationFactory can convert a mail address like string into a
 * {@link RecipientSpecification} instance.
 */
public class RecipientSpecificationFactory {
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
        RecipientAST recipientAST;
        try {
            recipientAST = new RecipientParser("<" + mailbox + ">").parse();
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
        if (recipientAST instanceof SystemPostmasterRecipientAST) {
            return new GlobalPostmasterSpecification();
        } else if (recipientAST instanceof DomainPostmasterRecipientAST) {
            DomainPostmasterRecipientAST domainPostmasterRecipientAST =
                    (DomainPostmasterRecipientAST) recipientAST;
            DomainPostmasterSpecification domainPostmaster =
                    new DomainPostmasterSpecification();

            RemotePart remotePart =
                    mailAddressFactory
                            .createRemotePartFromAST(domainPostmasterRecipientAST.mailboxAST.remotePartAST);
            domainPostmaster.setRemotePart(remotePart);
            return domainPostmaster;

        } else if (recipientAST instanceof MailboxRecipientAST) {
            MailboxRecipientAST mailboxRecipientAST =
                    (MailboxRecipientAST) recipientAST;
            LocalPartSpecification localPart =
                    new CaseInsensitiveLocalPartSpecification(
                            mailboxRecipientAST.pathAST.mailboxAST.localPartAST.spelling);
            RemotePart remotePart =
                    mailAddressFactory
                            .createRemotePartFromAST(mailboxRecipientAST.pathAST.mailboxAST.remotePartAST);
            return new LocalRemoteCombinedRecipientSpecification(localPart,
                    remotePart);
        } else {
            throw new RuntimeException("Assertion failed");
        }

    }
}
