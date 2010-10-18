package mireka.filter.local;

import java.util.regex.Pattern;

import mireka.address.Address;
import mireka.address.MailAddressFactory;
import mireka.address.RemotePart;
import mireka.address.RemotePartContainingRecipient;

public class RegexAddressSpecification implements RecipientSpecification {
    private String localPartRegex;
    private Pattern pattern;
    private RemotePart remotePartObject;
    private String remotePartString;

    @Override
    public boolean isSatisfiedBy(RemotePartContainingRecipient recipient) {
        Address recipientAddress = recipient.getAddress();
        boolean remotePartMatch =
                remotePartObject.equals(recipient.getAddress().getRemotePart());
        if (!remotePartMatch)
            return false;
        return pattern.matcher(recipientAddress.getLocalPart().toString())
                .matches();
    }

    @Override
    public String toString() {
        return "(Regex: " + localPartRegex + ")@" + remotePartString;
    }

    /**
     * @category GETSET
     */
    public String getLocalPartRegex() {
        return localPartRegex;
    }

    /**
     * @category GETSET
     */
    public void setLocalPartRegex(String localPartRegex) {
        this.localPartRegex = localPartRegex;
        this.pattern =
                Pattern.compile(localPartRegex, Pattern.CASE_INSENSITIVE
                        | Pattern.UNICODE_CASE);
    }

    /**
     * @category GETSET
     */
    public String getRemotePart() {
        return remotePartString;
    }

    /**
     * @category GETSET
     */
    public void setRemotePart(String remotePart) {
        this.remotePartString = remotePart;
        this.remotePartObject =
                new MailAddressFactory().createRemotePart(remotePart);
    }
}
