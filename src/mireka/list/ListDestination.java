package mireka.list;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import mireka.address.MailAddressFactory;
import mireka.address.Recipient;
import mireka.address.ReversePath;
import mireka.destination.MailDestination;
import mireka.smtp.RejectExceptionExt;
import mireka.transmission.Mail;
import mireka.transmission.Transmitter;

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
    final List<ListMember> members = new ArrayList<ListMember>();

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
    ReversePath reversePath;

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
    Transmitter transmitter;

    @PostConstruct
    public void setDefaults() {
        if (listId == null)
            listId = address.sourceRouteStripped().replace('@', '.');
    }

    @Override
    public void data(Mail mail) throws RejectExceptionExt {
        new Redistributor(mail, this).distribute();
    }

    String getShortListName() {
        return address.localPart().displayableName();
    }

    @Override
    public String toString() {
        return "ListDestination [" + address + "]";
    }

    /**
     * @x.category GETSET
     */
    public String getAddress() {
        return address.toString();
    }

    /**
     * @x.category GETSET
     */
    public void setAddress(String address) {
        try {
            this.address = new MailAddressFactory().createRecipient(address);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @x.category GETSET
     */
    public String getListId() {
        return listId;
    }

    /**
     * @x.category GETSET
     */
    public void setListId(String listId) {
        this.listId = listId;
    }

    /**
     * @x.category GETSET
     */
    public void addMember(ListMember listMember) {
        members.add(listMember);
    }

    /**
     * @x.category GETSET
     */
    public void setMembers(List<ListMember> members) {
        this.members.clear();
        this.members.addAll(members);
    }

    /**
     * @x.category GETSET
     */
    public List<ListMember> getMembers() {
        return this.members;
    }

    /**
     * @x.category GETSET
     */
    public String getSubjectPrefix() {
        return subjectPrefix;
    }

    /**
     * @x.category GETSET
     */
    public void setSubjectPrefix(String subjectPrefix) {
        this.subjectPrefix = subjectPrefix;
    }

    /**
     * @x.category GETSET
     */
    public boolean isMembersOnly() {
        return membersOnly;
    }

    /**
     * @x.category GETSET
     */
    public void setMembersOnly(boolean membersOnly) {
        this.membersOnly = membersOnly;
    }

    /**
     * @x.category GETSET
     */
    public boolean isAttachmentsAllowed() {
        return attachmentsAllowed;
    }

    /**
     * @x.category GETSET
     */
    public void setAttachmentsAllowed(boolean attachmentsAllowed) {
        this.attachmentsAllowed = attachmentsAllowed;
    }

    /**
     * @x.category GETSET
     */
    public boolean isReplyToList() {
        return replyToList;
    }

    /**
     * @x.category GETSET
     */
    public void setReplyToList(boolean replyToList) {
        this.replyToList = replyToList;
    }

    /**
     * @x.category GETSET
     */
    public String getReversePath() {
        return reversePath.getSmtpText();
    }

    /**
     * @x.category GETSET
     */
    public void setReversePath(String reversePath) {
        this.reversePath =
                new MailAddressFactory()
                        .createReversePathAlreadyVerified(reversePath);
    }

    /**
     * @x.category GETSET
     */
    public String getMembersOnlyMessage() {
        return membersOnlyMessage;
    }

    /**
     * @x.category GETSET
     */
    public void setMembersOnlyMessage(String membersOnlyMessage) {
        this.membersOnlyMessage = membersOnlyMessage;
    }

    /**
     * @x.category GETSET
     */
    public MailValidator getNonMemberSenderValidator() {
        return nonMemberSenderValidator;
    }

    /**
     * @x.category GETSET
     */
    public void setNonMemberSenderValidator(
            MailValidator nonMemberSenderValidator) {
        this.nonMemberSenderValidator = nonMemberSenderValidator;
    }

    /**
     * @x.category GETSET
     */
    public Transmitter getTransmitter() {
        return transmitter;
    }

    /**
     * @x.category GETSET
     */
    @Inject
    public void setTransmitter(Transmitter transmitter) {
        this.transmitter = transmitter;
    }
}
