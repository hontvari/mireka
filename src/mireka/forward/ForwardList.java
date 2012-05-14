package mireka.forward;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import mireka.address.MailAddressFactory;
import mireka.address.Recipient;
import mireka.filter.local.table.RecipientSpecification;
import mireka.filter.local.table.RecipientSpecificationFactory;
import mireka.smtp.RejectExceptionExt;
import mireka.transmission.LocalMailSystemException;
import mireka.transmission.Mail;
import mireka.transmission.Transmitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ForwardList redistributes mail to multiple recipients without changing the
 * reverse path.
 */
public class ForwardList {
    private final Logger logger = LoggerFactory.getLogger(ForwardList.class);

    /**
     * The address of the list as a Recipient.
     */
    @Nonnull
    private Recipient address;
    /**
     * The address of the list as a {@link RecipientSpecification}.
     */
    private RecipientSpecification recipientSpecification;
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

    /**
     * Processes the message.
     */
    public void submit(Mail srcMail) throws RejectExceptionExt {
        logger.debug("Mail is received for forward list {}: {}", address,
                srcMail);
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
    public String getAddress() {
        return address.toString();
    }

    /**
     * @category GETSET
     */
    public void setAddress(String address) {
        try {
            this.address = new MailAddressFactory().createRecipient(address);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        this.recipientSpecification =
                new RecipientSpecificationFactory().create(address);
    }

    /**
     * @category GETSET
     */
    public void addMember(Member member) {
        members.add(member);
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
    public Transmitter getTransmitter() {
        return transmitter;
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
    public RecipientSpecification getRecipientSpecification() {
        return recipientSpecification;
    }

    /**
     * @category GETSET
     */
    public Srs getSrs() {
        return srs;
    }

    /**
     * @category GETSET
     */
    public void setSrs(Srs srs) {
        this.srs = srs;
    }

}
