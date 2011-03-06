package mireka.smtp;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * A template for an SMTP reply to be sent. It is used to specify a custom reply
 * for specific event in the configuration.
 */
public class SmtpReplyTemplate {
    /**
     * 0 means default
     */
    public int code;
    /**
     * null means default
     */
    public String enhancedCode;
    /**
     * null means default
     */
    public String text;

    public SmtpReplyTemplate() {
        // use default values
    }

    public SmtpReplyTemplate(int code, String enhancedCode, String text) {
        this.code = code;
        this.enhancedCode = enhancedCode;
        this.text = text;
    }

    public void checkFullySpecified() throws IllegalArgumentException {
        if (code == 0)
            throw new IllegalArgumentException("Reply code is not specified");
        if (enhancedCode == null)
            throw new IllegalArgumentException(
                    "Enhanced status code is not specified");
        if (text == null)
            throw new IllegalArgumentException("Reply text is not specified");
    }

    public SmtpReplyTemplate format(Object... arguments) {
        MessageFormat format = new MessageFormat(text, Locale.US);
        String formattedText = format.format(arguments);
        return new SmtpReplyTemplate(code, enhancedCode, formattedText);
    }

    /**
     * @param replies
     *            first element must be the most specific
     * @return this
     */
    public SmtpReplyTemplate resolveDefaultsFrom(SmtpReplyTemplate... replies) {
        int actualCode = this.code;
        String actualEnhancedCode = this.enhancedCode;
        String actualText = this.text;
        for (SmtpReplyTemplate reply : replies) {
            if (reply == null)
                continue;
            if (actualCode == 0)
                actualCode = reply.code;
            if (actualEnhancedCode == null)
                actualEnhancedCode = reply.enhancedCode;
            if (actualText == null)
                actualText = reply.text;
        }
        if (actualCode == 0)
            throw new IllegalArgumentException();
        if (actualEnhancedCode == null)
            throw new IllegalArgumentException();
        if (actualText == null)
            throw new IllegalArgumentException();
        return new SmtpReplyTemplate(actualCode, actualEnhancedCode, actualText);
    }

    /**
     * Converts this fully specified object to an SMTP enhanced status message.
     */
    public EnhancedStatus toEnhancedStatus() {
        checkFullySpecified();
        return new EnhancedStatus(code, enhancedCode, text);
    }
}
