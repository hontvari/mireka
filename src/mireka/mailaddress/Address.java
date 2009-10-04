package mireka.mailaddress;

import javax.mail.internet.ParseException;

/**
 * Corresponds to the Mailbox production in RFC 5321.
 * 
 * This is a draft implementation. Apache Mailet has a seemingly complete
 * address implementation in the MailAddress class.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc5321#section-4.1.2">RFC 5321
 *      4.1.2</a>
 */
public class Address {
    private String mailbox;
    private LocalPart localPart;
    private RemotePart remotePart;

    public Address(String mailbox) {
        this.mailbox = mailbox;
        try {
            parse();
        } catch (ParseException e) {
            throw new RuntimeException(e); // unexpected
        }
    }

    public Address(String localPart, String remotePart) {
        this.mailbox = localPart + "@" + remotePart;
        createLocalPart(localPart);
        createRemotePart(remotePart);
    }

    private void parse() throws ParseException {
        int iLastAt = mailbox.lastIndexOf('@');
        if (iLastAt == -1)
            throw new ParseException();
        if (iLastAt == mailbox.length() - 1)
            throw new ParseException();
        String localString = mailbox.substring(0, iLastAt);
        String remoteString = mailbox.substring(iLastAt + 1);
        createLocalPart(localString);
        createRemotePart(remoteString);
    }

    private void createLocalPart(String localPartString) {
        this.localPart = new LocalPart(localPartString);
    }

    private void createRemotePart(String remotePartString) {
        if (remotePartString.startsWith("["))
            this.remotePart = new AddressLiteral(remotePartString);
        else
            this.remotePart = new DomainPart(remotePartString);
    }

    public LocalPart getLocalPart() {
        return localPart;
    }

    public RemotePart getRemotePart() {
        return remotePart;
    }

    @Override
    public String toString() {
        return mailbox;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result =
                prime * result
                        + ((remotePart == null) ? 0 : remotePart.hashCode());
        result =
                prime * result
                        + ((localPart == null) ? 0 : localPart.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Address other = (Address) obj;
        if (remotePart == null) {
            if (other.remotePart != null)
                return false;
        } else if (!remotePart.equals(other.remotePart))
            return false;
        if (localPart == null) {
            if (other.localPart != null)
                return false;
        } else if (!localPart.equals(other.localPart))
            return false;
        return true;
    }
}
