package mireka;

import java.text.MessageFormat;
import java.util.Locale;

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
