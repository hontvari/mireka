package mireka.address;

/**
 * Corresponds to the Mailbox production in RFC 5321, basically a
 * LOCAL_PART@REMOTE_PART element.
 * <p>
 * Note: RFC 5322 Internet Message Format also contains a mailbox production,
 * but with different content. The addr-spec production of that RFC is the
 * production which corresponds to this class.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc5321#section-4.1.2">RFC 5321
 *      4.1.2</a>
 */
public class Mailbox {
    private String smtpText;
    private LocalPart localPart;
    private RemotePart remotePart;

    public Mailbox(String smtpText, LocalPart localPart, RemotePart remotePart) {
        this.smtpText = smtpText;
        this.localPart = localPart;
        this.remotePart = remotePart;
    }

    public LocalPart getLocalPart() {
        return localPart;
    }

    public RemotePart getRemotePart() {
        return remotePart;
    }

    /**
     * Returns the raw mailbox, as it was supplied in the SMTP transaction.
     */
    public String getSmtpText() {
        return smtpText;
    }

    /**
     * Returns the displayable form of the address.
     */
    @Override
    public String toString() {
        return smtpText;
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
        Mailbox other = (Mailbox) obj;
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
