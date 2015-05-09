package mireka.filter.local.table;

import java.util.regex.Pattern;

import mireka.smtp.address.MailAddressFactory;
import mireka.smtp.address.Recipient;
import mireka.smtp.address.RemotePart;
import mireka.smtp.address.RemotePartContainingRecipient;

/**
 * RegexAddressSpecification compares the local part of a recipient address with
 * a regular expression and it also requires the remote part to be identical
 * with the specified remote part.
 */
public class RegexAddressSpecification implements RecipientSpecification {
    private String localPartRegex;
    private Pattern pattern;
    private RemotePart remotePartObject;
    private String remotePartString;

    @Override
    public boolean isSatisfiedBy(Recipient recipient) {
        if (!(recipient instanceof RemotePartContainingRecipient))
            return false;

        RemotePart recipientRemotePart =
                ((RemotePartContainingRecipient) recipient).getMailbox()
                        .getRemotePart();
        if (!remotePartObject.equals(recipientRemotePart))
            return false;
        return pattern.matcher(recipient.localPart().displayableName())
                .matches();
    }

    @Override
    public String toString() {
        return "(Regex: " + localPartRegex + ")@" + remotePartString;
    }

    /**
     * @x.category GETSET
     */
    public String getLocalPartRegex() {
        return localPartRegex;
    }

    /**
     * @x.category GETSET
     */
    public void setLocalPartRegex(String localPartRegex) {
        this.localPartRegex = localPartRegex;
        this.pattern =
                Pattern.compile(localPartRegex, Pattern.CASE_INSENSITIVE
                        | Pattern.UNICODE_CASE);
    }

    /**
     * @x.category GETSET
     */
    public String getRemotePart() {
        return remotePartString;
    }

    /**
     * @x.category GETSET
     */
    public void setRemotePart(String remotePart) {
        this.remotePartString = remotePart;
        this.remotePartObject =
                new MailAddressFactory()
                        .createRemotePartFromDisplayableText(remotePart);
    }
}
