package mireka.forward;

import java.util.ArrayList;
import java.util.List;

import mireka.destination.MailDestination;
import mireka.smtp.RejectExceptionExt;
import mireka.transmission.LocalMailSystemException;
import mireka.transmission.Mail;
import mireka.transmission.Transmitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ForwardDestination redistributes mail to multiple recipients without changing
 * the reverse path.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc5321#section-3.9">RFC 5321 3.9
 *      Mailing Lists and Aliases</a>
 */
public class ForwardDestination implements MailDestination {
    private Logger logger = LoggerFactory.getLogger(ForwardDestination.class);

    /**
     * Mails sent to {@link #address} will be redistributed to the member
     * addresses in this list.
     */
    private final List<Member> members = new ArrayList<Member>();

    /**
     * The transmitter which will be used to redistribute the incoming mail to
     * the members.
     */
    private Transmitter transmitter;

    private Srs srs;

    @Override
    public void data(Mail mail) throws RejectExceptionExt {
        forward(mail);
    }

    /**
     * Processes the message.
     */
    public void forward(Mail srcMail) throws RejectExceptionExt {
        logger.debug("Mail is received for {} from {}", toString(), srcMail);
        if (members.isEmpty()) {
            logger.debug("Forward list has no members, dropping mail");
            return;
        }

        Mail mail = srcMail.copy();
        mail.recipients.clear();
        for (Member member : members) {
            if (member.isDisabled())
                continue;
            mail.recipients.add(member.getRecipient());
        }
        try {
            mail.from = srs.forward(mail.from, srcMail.recipients.get(0));
            transmitter.transmit(mail);
            logger.debug("Forward list mail was submitted to transmitter: {}",
                    mail);
        } catch (LocalMailSystemException e) {
            logger.error("Cannot transmit mail", e);
            throw new RejectExceptionExt(e.errorStatus());
        }
    }

    /**
     * @category GETSET
     */
    public void setMembers(List<Member> members) {
        this.members.clear();
        this.members.addAll(members);
    }

    /**
     * @category GETSET
     */
    public void setTransmitter(Transmitter transmitter) {
        this.transmitter = transmitter;
    }

    /**
     * @category GETSET
     */
    public void setSrs(Srs srs) {
        this.srs = srs;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        if (members.size() >= 1)
            buffer.append(members.get(0));
        if (members.size() >= 2)
            buffer.append(", ").append(members.get(1));
        if (members.size() >= 3)
            buffer.append(", …");
        return "ForwardDestination [" + buffer + "]";
    }
}
