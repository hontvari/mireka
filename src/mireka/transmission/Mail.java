package mireka.transmission;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mireka.MailData;
import mireka.address.Recipient;
import mireka.address.ReversePath;

/**
 * An SMTP mail object, which contains both an envelope and content.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc5321#section-2.3.1">RFC 5321
 *      Simple Mail Transfer Protocol</a>
 */
public class Mail {
    @Nonnull
    public ReversePath from;
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
     * The desired date of sending this mail. Null means immediately.
     */
    public Date scheduleDate;
    /**
     * Count of failed attempts until now.
     */
    public int deliveryAttempts;
    /**
     * Count of postponings of delivery attempts since the last actually
     * performed attempt. Postponing a delivery means that no remote SMTP hosts
     * were connected, so a postponed delivery attempt must not be considered as
     * a retry.
     */
    public int postpones;

    /**
     * Creates an essentially deep copy of this instance. The same
     * {@link #mailData} object is used, otherwise every other field is a deep
     * copy.
     * 
     * @return A deep copy of this mail, except the {@link #mailData} object,
     *         which is used in both the new and in this object.
     */
    public Mail copy() {
        Mail result = new Mail();
        result.from = from;
        result.recipients.addAll(recipients);
        result.mailData = mailData;
        result.arrivalDate = arrivalDate;
        result.receivedFromMtaName = receivedFromMtaName;
        result.receivedFromMtaAddress = receivedFromMtaAddress;
        result.scheduleDate = scheduleDate;
        result.deliveryAttempts = deliveryAttempts;
        result.postpones = postpones;
        return result;
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
        } else if (recipients.size() == 1) {
            builder.append(", recipient=");
            builder.append(recipients.get(0));
        } else {
            builder.append(", no recipients");
        }
        builder.append("]");
        return builder.toString();
    }

}
