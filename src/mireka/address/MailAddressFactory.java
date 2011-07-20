package mireka.address;

import java.text.ParseException;

import mireka.address.parser.RecipientParser;
import mireka.address.parser.RemotePartParser;
import mireka.address.parser.ast.AddressLiteralRemotePartAST;
import mireka.address.parser.ast.DomainPostmasterRecipientAST;
import mireka.address.parser.ast.DomainRemotePartAST;
import mireka.address.parser.ast.MailboxAST;
import mireka.address.parser.ast.MailboxRecipientAST;
import mireka.address.parser.ast.SystemPostmasterRecipientAST;

/**
 * MailAddressFactory creates mail address related objects from strings.
 */
public class MailAddressFactory {
    /**
     * Parses the specified string and creates a {@link Recipient} instance.
     * 
     * @param recipientString
     *            the recipient parameter of the RCPT SMTP command, without the
     *            enclosing angle bracket.
     */
    public mireka.address.Recipient createRecipient(String recipientString)
            throws ParseException {
        String recipientStringWithAngleBracket = "<" + recipientString + ">";
        mireka.address.parser.ast.RecipientAST recipientAST =
                new RecipientParser(recipientStringWithAngleBracket).parse();
        if (recipientAST instanceof SystemPostmasterRecipientAST) {
            SystemPostmasterRecipientAST systemPostmasterAST =
                    (SystemPostmasterRecipientAST) recipientAST;
            return new GlobalPostmaster(systemPostmasterAST.postmasterSpelling);
        } else if (recipientAST instanceof DomainPostmasterRecipientAST) {
            DomainPostmasterRecipientAST domainPostmasterAST =
                    (DomainPostmasterRecipientAST) recipientAST;
            Mailbox mailbox = createMailbox(domainPostmasterAST.mailboxAST);
            return new DomainPostmaster(mailbox);
        } else if (recipientAST instanceof MailboxRecipientAST) {
            MailboxRecipientAST mailboxRecipientAST =
                    (MailboxRecipientAST) recipientAST;
            Mailbox mailbox = createMailbox(mailboxRecipientAST.pathAST.mailboxAST);
            return new GenericRecipient(mailbox);
        } else {
            throw new RuntimeException("Assertion failed");
        }
    }

    private Mailbox createMailbox(MailboxAST mailboxAST) {
        LocalPart localPart = new LocalPart(mailboxAST.localPartAST.spelling);
        RemotePart remotePart = createRemotePartFromAST(mailboxAST.remotePartAST);
        return new Mailbox(mailboxAST.spelling, localPart, remotePart);
    }

    public mireka.address.RemotePart createRemotePartFromAST(
            mireka.address.parser.ast.RemotePartAST remotePartAST) {
        RemotePart remotePart;
        if (remotePartAST instanceof DomainRemotePartAST) {
            DomainRemotePartAST domainRemotePartAST =
                    (DomainRemotePartAST) remotePartAST;
            Domain domain = new Domain(domainRemotePartAST.spelling);
            remotePart = new DomainPart(domain);
        } else if (remotePartAST instanceof AddressLiteralRemotePartAST) {
            AddressLiteralRemotePartAST addressLiteralRemotePartAST =
                    (AddressLiteralRemotePartAST) remotePartAST;
            remotePart =
                    new AddressLiteral(addressLiteralRemotePartAST.spelling,
                            addressLiteralRemotePartAST.address);
        } else {
            throw new RuntimeException("Assertion failed");
        }
        return remotePart;
    }

    public Recipient createRecipientAlreadyVerified(String recipientString) {
        try {
            return createRecipient(recipientString);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Unexpected exception", e);
        }
    }

    /**
     * Creates a {@link RemotePart} object by parsing the specified displayable
     * text. Note that currently actual parsing of displayable text is not
     * implemented, internationalized domain names must be specified in ASCII
     * compatible text.
     */
    public RemotePart createRemotePartFromDisplayableText(String displayableText) {
        mireka.address.parser.ast.RemotePartAST remotePartAST;
        try {
            remotePartAST = new RemotePartParser(displayableText).parse();
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
        return createRemotePartFromAST(remotePartAST);
    }
}
