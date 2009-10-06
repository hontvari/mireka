package mireka.mailaddress;

import java.util.Locale;

import javax.mail.internet.ParseException;

public class MailAddressFactory {
    public Recipient createRecipient(String recipientString)
            throws ParseException {
        if (isGlobalPostmaster(recipientString))
            return new GlobalPostmaster(recipientString);
        else if (isDomainPostmaster(recipientString))
            return new DomainPostmaster(recipientString);
        else
            return new AddressRecipient(recipientString);
    }

    private boolean isGlobalPostmaster(String recipient) {
        String lowerCase = recipient.toLowerCase(Locale.US);
        return "postmaster".equals(lowerCase);
    }

    private boolean isDomainPostmaster(String recipient) throws ParseException {
        String prefix = "postmaster@";
        String lowerCase = recipient.toLowerCase(Locale.US);
        if (!lowerCase.startsWith(prefix))
            return false;
        if (lowerCase.length() == prefix.length())
            throw new ParseException();
        return true;
    }
    
    public RemotePart createRemotePart(String remotePartString) {
        if (remotePartString.length() == 0)
            throw new IllegalArgumentException();
        if (remotePartString.startsWith("["))
            return new AddressLiteral(remotePartString);
        else
            return new DomainPart(remotePartString);
    }
}
