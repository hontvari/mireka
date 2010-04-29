package mireka.transmission;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mireka.MailData;
import mireka.address.Recipient;

/**
 * An SMTP mail object, which contains both an envelope and content.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc5321#section-2.3.1">RFC 5321
 *      Simple Mail Transfer Protocol</a>
 */
public class Mail {
    /**
     * empty string for null reverse-path
     */
    @Nonnull
    public String from;
    @Nonnull
    public List<Recipient> recipients = new ArrayList<Recipient>();
    public MailData mailData;
    /**
     * If the message was generated locally then this time should be the date of
     * creation.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3464#section-2.2.5">rfc3464 -
     *      2.2.5 The Arrival-Date DSN field</a>
     */
    @Nonnull
    public Date arrivalDate;
    /**
     * HELO or EHLO name, null if not received
     */
    @Nullable
    public String receivedFromMtaName;
    /**
     * null if the mail was generated locally
     */
    @Nullable
    public InetAddress receivedFromMtaAddress;

    /**
     * the desired date of sending this mail. Null means immediately.
     */
    public Date scheduleDate;
    /**
     * count of failed attempts until now
     */
    public int deliveryAttempts;

    public Mail() {
        // 
    }

    public Mail(String from, List<Recipient> recipients, MailData mailData) {
        this.from = from;
        this.recipients.addAll(recipients);
        this.mailData = mailData;
    }

    public Mail(Mail src, MailData mailData) {
        this.from = src.from;
        this.recipients.addAll(src.recipients);
        this.mailData = mailData;
        this.scheduleDate = src.scheduleDate;
        this.deliveryAttempts = src.deliveryAttempts;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Mail [");
        builder.append("from=");
        builder.append(from);
        if (recipients.size() > 1) {
            builder.append(", recipients=");
            builder.append(recipients.get(0));
            builder.append(",...");
        } else {
            builder.append(", recipient=");
            builder.append(recipients.get(0));
        }
        builder.append("]");
        return builder.toString();
    }

}
