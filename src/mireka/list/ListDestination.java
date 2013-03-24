package mireka.list;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import mireka.address.MailAddressFactory;
import mireka.address.Recipient;
import mireka.address.ReversePath;
import mireka.destination.MailDestination;
import mireka.smtp.EnhancedStatus;
import mireka.smtp.RejectExceptionExt;
import mireka.transmission.LocalMailSystemException;
import mireka.transmission.Mail;
import mireka.transmission.Transmitter;
import mireka.util.MimeMessageConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ListDestination assigned to a recipient indicates that the mail should be
 * redistributed to multiple recipients.
 * <p>
 * This is a very simple mailing list implementation suitable for small, closed,
 * internal lists.
 * <p>
 * Most of this class is coming from the GenericListserv class of Apache James.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc5321#section-3.9">RFC 5321 3.9
 *      Mailing Lists and Aliases</a>
 */
public class ListDestination implements MailDestination {

    private final Logger logger = LoggerFactory
            .getLogger(ListDestination.class);

    /**
     * The address of the list as a Recipient.
     */
    @Nonnull
    private Recipient address;

    /**
     * Unique identifier of the list, used in the List-Id header. For example:
     * games.example.com<br>
     */
    private String listId;

    /**
     * Mails sent to {@link #address} will be redistributed to the member
     * addresses in this list.
     */
    private final List<ListMember> members = new ArrayList<ListMember>();

    /**
     * A prefix that will be inserted at the front of the subject. Null means no
     * prefix.
     */
    private String subjectPrefix = null;

    /**
     * if true, only members can post to the list.
     */
    private boolean membersOnly = true;

    /**
     * If false, attachments are not allowed.
     */
    private boolean attachmentsAllowed = true;

    /**
     * If true, replies go back to the list address; if false they go to the
     * sender.
     */
    private boolean replyToList = true;

    /**
     * The mail address which will be used as the return address of the mail
     * sent to the list members (SMTP envelope MAIL FROM). This address will get
     * the bounces. Default is the postmaster address corresponding to the
     * remote part of the {@link #address} field.
     */
    @Nonnull
    private ReversePath reversePath;

    /**
     * If supplied then it overrides the default error message used when a
     * non-member posts to a members only list.
     * 
     */
    private String membersOnlyMessage =
            "Only members of the list are allowed to send a message "
                    + "to this list address.";

    /**
     * The validator may decide that the mail must be accepted even if the
     * sender is not a member of the list.
     */
    private MailValidator nonMemberSenderValidator = null;

    /**
     * The transmitter which will be used to redistribute the incoming mail to
     * the members.
     */
    private Transmitter transmitter;
    
    @PostConstruct
    public void setDefaults() {
        if (listId == null)
            listId = address.sourceRouteStripped().replace('@', '.');
    }

    @Override
    public void data(Mail mail) throws RejectExceptionExt {
        redistribute(mail);
    }

    /**
     * Processes the message.
     */
    public void redistribute(Mail srcMail) throws RejectExceptionExt {
        logger.debug("Mail is received for {} from {}", toString(), srcMail);
        ParsedMail mail = new ParsedMail(srcMail);
        checkSender(mail);
        checkAttachmentsAllowed(mail);
        MimeMessage outgoingMessage = createOutgoingMimeMessage(mail);
        sendMail(srcMail, outgoingMessage);
    }

    private void checkSender(ParsedMail mail) throws RejectExceptionExt {
        if (!membersOnly)
            return;
        if (isMember(mail.getMail().from))
            return;
        if (nonMemberSenderValidator != null
                && nonMemberSenderValidator.shouldBeAccepted(mail))
            return;
        throw new RejectExceptionExt(new EnhancedStatus(550, "5.7.2",
                membersOnlyMessage));
    }

    private boolean isMember(ReversePath reversePath) {
        for (ListMember listMember : members) {
            if (listMember.getRecipient().toString()
                    .equalsIgnoreCase(reversePath.toString()))
                return true;
        }
        return false;
    }

    private void checkAttachmentsAllowed(ParsedMail mail)
            throws RejectExceptionExt {
        if (attachmentsAllowed)
            return;

        try {
            if (mail.getMimeMessage().getContent() instanceof MimeMultipart) {
                throw new RejectExceptionExt(new EnhancedStatus(550, "5.7.0",
                        "Attachments are not allowed on this mailing list"));
            }
        } catch (IOException e) {
            logger.error("Cannot get content of a mail", e);
            throw new RejectExceptionExt(EnhancedStatus.BAD_MESSAGE_BODY);
        } catch (MessagingException e) {
            logger.error("Message content cannot be parsed", e);
            throw new RejectExceptionExt(EnhancedStatus.BAD_MESSAGE_BODY);
        }
    }

    private MimeMessage createOutgoingMimeMessage(ParsedMail mail)
            throws RejectExceptionExt {
        try {
            // Create a copy of this message to send out
            MimeMessage outgoingMessage =
                    new MimeMessage(mail.getMimeMessage());
            // We need to remove this header from the copy we're sending around
            outgoingMessage.removeHeader("Return-Path");

            // Check if the X-been-there header is set to the listserv's name
            // (the address). If it has, this means it's a message from this
            // listserv that's getting bounced back, so we need to swallow it
            if (address.toString().equals(
                    outgoingMessage.getHeader("X-been-there"))) {
                throw new RejectExceptionExt(new EnhancedStatus(450, "4.4.6",
                        "Mail list loop detected"));
            }

            setSubject(outgoingMessage);

            // If replies should go to this list, we need to set the header
            if (replyToList) {
                outgoingMessage.setHeader("Reply-To", address.toString());
            }
            // We're going to set this special header to avoid bounces
            // getting sent back out to the list
            outgoingMessage.setHeader("X-been-there", address.toString());

            outgoingMessage.setHeader("List-Id", "<" + listId + ">");
            outgoingMessage.setHeader("List-Post", "<" + address + ">");
            outgoingMessage.removeHeader("List-Help");
            outgoingMessage.removeHeader("List-Unsubscribe");
            outgoingMessage.removeHeader("List-Subscribe");
            outgoingMessage.removeHeader("List-Owner");
            outgoingMessage.removeHeader("List-Archive");

            return outgoingMessage;
        } catch (MessagingException e) {
            logger.error("Cannot create a mail list MimeMessage", e);
            throw new RejectExceptionExt(
                    EnhancedStatus.TRANSIENT_LOCAL_ERROR_IN_PROCESSING);
        }
    }

    private void setSubject(MimeMessage outgoingMessage)
            throws MessagingException {
        if (subjectPrefix == null)
            return;
        String subj = outgoingMessage.getSubject();
        if (subj == null) {
            subj = "";
        }
        subj = normalizeSubject(subj, subjectPrefix);
        outgoingMessage.setSubject(subj, "UTF-8");
    }

    /**
     * <p>
     * This takes the subject string and reduces (normalizes) it. Multiple "Re:"
     * entries are reduced to one, and capitalized. The prefix is always
     * moved/placed at the beginning of the line, and extra blanks are reduced,
     * so that the output is always of the form:
     * </p>
     * <code>
     * &lt;prefix&gt; + &lt;one-optional-"Re:"*gt; + &lt;remaining subject&gt;
     * </code>
     * <p>
     * I have done extensive testing of this routine with a standalone driver,
     * and am leaving the commented out debug messages so that when someone
     * decides to enhance this method, it can be yanked it from this file,
     * embedded it with a test driver, and the comments enabled.
     * </p>
     */
    private static String normalizeSubject(final String subj,
            final String prefix) {
        StringBuilder subject = new StringBuilder(subj);
        int prefixLength = prefix.length();

        // System.err.println("In:  " + subject);

        // If the "prefix" is not at the beginning the subject line, remove it
        int index = subject.indexOf(prefix);
        if (index != 0) {
            // System.err.println("(p) index: " + index + ", subject: " +
            // subject);
            if (index > 0) {
                subject.delete(index, index + prefixLength);
            }
            subject.insert(0, prefix + " "); // insert prefix at the front
        }

        // Replace Re: with RE:
        String match = "Re:";
        index = subject.indexOf(match, prefixLength);

        while (index > -1) {
            // System.err.println("(a) index: " + index + ", subject: " +
            // subject);
            subject.replace(index, index + match.length(), "RE:");
            index = subject.indexOf(match, prefixLength);
            // System.err.println("(b) index: " + index + ", subject: " +
            // subject);
        }

        // Reduce them to one at the beginning
        match = "RE:";
        int indexRE = subject.indexOf(match, prefixLength) + match.length();
        index = subject.indexOf(match, indexRE);
        while (index > 0) {
            // System.err.println("(c) index: " + index + ", subject: " +
            // subject);
            subject.delete(index, index + match.length());
            index = subject.indexOf(match, indexRE);
            // System.err.println("(d) index: " + index + ", subject: " +
            // subject);
        }

        // Reduce blanks
        match = "  ";
        index = subject.indexOf(match, prefixLength);
        while (index > -1) {
            // System.err.println("(e) index: " + index + ", subject: " +
            // subject);
            subject.replace(index, index + match.length(), " ");
            index = subject.indexOf(match, prefixLength);
            // System.err.println("(f) index: " + index + ", subject: " +
            // subject);
        }

        // System.err.println("Out: " + subject);

        return subject.toString();
    }

    private void sendMail(Mail srcMail, MimeMessage mimeMessage)
            throws RejectExceptionExt {
        Mail mail = new Mail();
        mail.from = reversePath;
        for (ListMember member : members) {
            if (member.isDisabled() || member.isNoDelivery())
                continue;
            mail.recipients.add(member.getRecipient());
        }
        if (mail.recipients.isEmpty()) {
            logger.debug("Mail list has no such members, "
                    + "who should receive mail, dropping mail");
            return;
        }

        mail.mailData =
                new MimeMessageConverter()
                        .createMailDataInSmtpSession(mimeMessage);
        try {
            mail.arrivalDate = srcMail.arrivalDate;
            mail.scheduleDate = mail.arrivalDate; // try to preserve order
            transmitter.transmit(mail);
            logger.debug("Mailing list mail was submitted to transmitter: {}",
                    mail);
        } catch (LocalMailSystemException e) {
            logger.error("Cannot transmit mail", e);
            throw new RejectExceptionExt(e.errorStatus());
        } finally {
            mail.mailData.dispose();
        }
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
        return "ListDestination [" + address + "]";
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
    }

    /**
     * @category GETSET
     */
    public String getListId() {
        return listId;
    }

    /**
     * @category GETSET
     */
    public void setListId(String listId) {
        this.listId = listId;
    }

    /**
     * @category GETSET
     */
    public void addMember(ListMember listMember) {
        members.add(listMember);
    }

    /**
     * @category GETSET
     */
    public void setMembers(List<ListMember> members) {
        this.members.clear();
        this.members.addAll(members);
    }

    /**
     * @category GETSET
     */
    public String getSubjectPrefix() {
        return subjectPrefix;
    }

    /**
     * @category GETSET
     */
    public void setSubjectPrefix(String subjectPrefix) {
        this.subjectPrefix = subjectPrefix;
    }

    /**
     * @category GETSET
     */
    public boolean isMembersOnly() {
        return membersOnly;
    }

    /**
     * @category GETSET
     */
    public void setMembersOnly(boolean membersOnly) {
        this.membersOnly = membersOnly;
    }

    /**
     * @category GETSET
     */
    public boolean isAttachmentsAllowed() {
        return attachmentsAllowed;
    }

    /**
     * @category GETSET
     */
    public void setAttachmentsAllowed(boolean attachmentsAllowed) {
        this.attachmentsAllowed = attachmentsAllowed;
    }

    /**
     * @category GETSET
     */
    public boolean isReplyToList() {
        return replyToList;
    }

    /**
     * @category GETSET
     */
    public void setReplyToList(boolean replyToList) {
        this.replyToList = replyToList;
    }

    /**
     * @category GETSET
     */
    public String getReversePath() {
        return reversePath.getSmtpText();
    }

    /**
     * @category GETSET
     */
    public void setReversePath(String reversePath) {
        this.reversePath =
                new MailAddressFactory()
                        .createReversePathAlreadyVerified(reversePath);
    }

    /**
     * @category GETSET
     */
    public String getMembersOnlyMessage() {
        return membersOnlyMessage;
    }

    /**
     * @category GETSET
     */
    public void setMembersOnlyMessage(String membersOnlyMessage) {
        this.membersOnlyMessage = membersOnlyMessage;
    }

    /**
     * @category GETSET
     */
    public MailValidator getNonMemberSenderValidator() {
        return nonMemberSenderValidator;
    }

    /**
     * @category GETSET
     */
    public void setNonMemberSenderValidator(
            MailValidator nonMemberSenderValidator) {
        this.nonMemberSenderValidator = nonMemberSenderValidator;
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
    @Inject
    public void setTransmitter(Transmitter transmitter) {
        this.transmitter = transmitter;
    }
}
