package mireka.smtp.address;

/**
 * NullReversePath corresponds to the empty adress in the SMTP MAIL command. It
 * means that no bounce messages should be sent, because the current message
 * itself is a bounce message.
 */
public class NullReversePath implements ReversePath {

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public String getSmtpText() {
        return "";
    }

    @Override
    public String toString() {
        return "<>";
    }

}
