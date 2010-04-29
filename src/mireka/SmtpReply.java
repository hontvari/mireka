package mireka;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * the SMTP reply which was sent or which is to be sent by an SMTP server, or a
 * template for such a reply to be sent. In the latter case it is used to
 * specify a custom reply for specific event in the configuration.
 * <p>
 * TODO: separate these two responsibility into two classes. Use the
 * {@link org.subethamail.smtp.client.SMTPClient.Response} class.
 */
public class SmtpReply {
    /**
     * 0 means default
     */
    public int code;
    /**
     * null means default
     */
    public String text;

    public SmtpReply() {
        // use default values
    }

    public SmtpReply(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public void checkFullySpecified() throws IllegalArgumentException {
        if (code == 0)
            throw new IllegalArgumentException("Reply code is not specified");
        if (text == null)
            throw new IllegalArgumentException("Reply text is not specified");
    }

    public SmtpReply format(Object... arguments) {
        MessageFormat format = new MessageFormat(text, Locale.US);
        String formattedText = format.format(arguments);
        return new SmtpReply(code, formattedText);
    }

    /**
     * @param replies
     *            first element must be the most specific
     * @return this
     */
    public SmtpReply resolveDefaultsFrom(SmtpReply... replies) {
        int actualCode = this.code;
        String actualText = this.text;
        for (SmtpReply reply : replies) {
            if (reply == null)
                continue;
            if (actualCode == 0)
                actualCode = reply.code;
            if (actualText == null)
                actualText = reply.text;
        }
        if (actualCode == 0)
            throw new IllegalArgumentException();
        if (actualText == null)
            throw new IllegalArgumentException();
        return new SmtpReply(actualCode, actualText);
    }
}
