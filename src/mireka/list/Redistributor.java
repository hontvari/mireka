package mireka.list;

import static mireka.maildata.FieldDef.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mireka.ConfigurationException;
import mireka.address.ReversePath;
import mireka.dmarc.PolicyDiscovery;
import mireka.dmarc.PolicyRecord;
import mireka.dmarc.PolicyRecord.Request;
import mireka.dmarc.RecoverableDmarcException;
import mireka.maildata.AddrSpec;
import mireka.maildata.Address;
import mireka.maildata.DomainPart;
import mireka.maildata.DotAtomDomainPart;
import mireka.maildata.LiteralDomainPart;
import mireka.maildata.Mailbox;
import mireka.maildata.Maildata;
import mireka.maildata.MediaType;
import mireka.maildata.field.UnstructuredField;
import mireka.smtp.EnhancedStatus;
import mireka.smtp.RejectExceptionExt;
import mireka.transmission.LocalMailSystemException;
import mireka.transmission.Mail;
import mireka.util.AssertionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Redistributor {
    private final Logger logger = LoggerFactory.getLogger(Redistributor.class);

    private Mail source;
    private ListDestination list;
    private Maildata newMaildata;

    public Redistributor(Mail source, ListDestination list) {
        this.source = source;
        this.list = list;
    }

    public void distribute() throws RejectExceptionExt {
        try {
            logger.debug(
                    "Mail is received for MailingListDestination [{}] from {}",
                    list.getAddress(), source);
            checkPermission();
            checkAttachmentsAllowed();
            checkListLoop();
            setupNewMaildata();
            sendMail();
        } catch (IOException e) {
            // Incoming messages are already saved to disk, so only hardware
            // failure can cause read error.
            logger.error("Cannot write maildata", e);
            throw new RejectExceptionExt(
                    EnhancedStatus.TRANSIENT_LOCAL_ERROR_IN_PROCESSING);
        } catch (ParseException e) {
            throw new RejectExceptionExt(EnhancedStatus.BAD_MESSAGE_BODY);
        } finally {
            if (newMaildata != null) {
                newMaildata.close();
            }
        }
    }

    private void checkPermission() throws RejectExceptionExt {
        if (!list.isMembersOnly())
            return;
        if (isMember(source.from))
            return;
        if (list.getNonMemberSenderValidator() != null
                && list.getNonMemberSenderValidator().shouldBeAccepted(source))
            return;
        throw new RejectExceptionExt(new EnhancedStatus(550, "5.7.2",
                list.getMembersOnlyMessage()));
    }

    private boolean isMember(ReversePath reversePath) {
        for (ListMember listMember : list.getMembers()) {
            if (listMember.getRecipient().toString()
                    .equalsIgnoreCase(reversePath.toString()))
                return true;
        }
        return false;
    }

    private void checkAttachmentsAllowed() throws RejectExceptionExt {
        if (list.isAttachmentsAllowed())
            return;

        if (source.maildata.getMediaType().equalTypeIdentifiers(
                MediaType.MULTIPART_MIXED))
            throw new RejectExceptionExt(new EnhancedStatus(550, "5.7.0",
                    "Attachments are not allowed on this mailing list"));
    }

    /**
     * Throws a RejectException if any List-Id field is present in the mail,
     * whether its own or a foreign id. According to RFC 2919 nested lists must
     * have explicit knowledge about a parent list. A list should not pass a
     * list mail from an unexpected source.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc2919">RFC 2919</a>
     */
    private void checkListLoop() throws RejectExceptionExt {
        if (source.maildata.headers().contains(LIST_ID))
            throw new RejectExceptionExt(new EnhancedStatus(450, "4.4.6",
                    "Mail list loop detected"));

    }

    private void setupNewMaildata() throws IOException, RejectExceptionExt,
            ParseException {
        newMaildata = source.maildata.copy();

        // We need to remove this header from the copy we're sending around
        newMaildata.headers().remove(RETURN_PATH);

        setupSubject();
        setupReplyTo();
        setupListFields();
        mangleAddressesForDmarc();
    }

    private void setupSubject() {
        if (list.getSubjectPrefix() == null)
            return;
        String subj = newMaildata.getSubject();
        subj = normalizeSubject(subj, list.getSubjectPrefix());
        newMaildata.setSubject(subj);
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

    private void setupReplyTo() {
        // If replies should go to this list, we need to set the header
        if (list.isReplyToList()) {
            Mailbox mailbox = new Mailbox();
            mailbox.displayName = list.getShortListName();
            mailbox.addrSpec = getListAddrSpec();
            newMaildata.setReplyToAddresses(Collections
                    .<Address> singletonList(mailbox));
        }
    }

    private void setupListFields() {
        newMaildata.headers().put(
                new UnstructuredField(LIST_ID, " <" + list.getListId() + ">"));
        newMaildata.headers()
                .put(new UnstructuredField(LIST_POST, " <" + list.getAddress()
                        + ">"));
        newMaildata.headers().remove(LIST_HELP);
        newMaildata.headers().remove(LIST_SUBSCRIBE);
        newMaildata.headers().remove(LIST_UNSUBSCRIBE);
        newMaildata.headers().remove(LIST_OWNER);
        // This list does not maintain an archive so it must not remove
        // List-Archive (if it would be a nested list).
    }

    /**
     * @see <a
     *      href="http://dmarc.org/supplemental/mailman-project-mlm-dmarc-reqs.html">DRAFT:
     *      Requirements Doc for MLM Patches to Support Basic DMARC
     *      Compliance</a>
     */
    private void mangleAddressesForDmarc() throws RejectExceptionExt,
            ParseException {
        List<Address> fromAddresses = newMaildata.getFromAddresses();
        List<Mailbox> mangledMailboxes = new ArrayList<>();

        for (int i = 0; i < fromAddresses.size(); i++) {
            Address a = fromAddresses.get(i);
            if (a instanceof Mailbox) {
                Mailbox m = (Mailbox) a;
                if (shouldMangeMailboxForDmarc(m)) {
                    mangledMailboxes.add(m);
                    fromAddresses.set(i, mangleMailbox(m));
                }
            } else if (a instanceof mireka.maildata.Group) {
                mireka.maildata.Group g = (mireka.maildata.Group) a;
                List<Mailbox> groupAddresses = g.mailboxList;
                for (int j = 0; j < groupAddresses.size(); j++) {
                    Mailbox m = groupAddresses.get(j);
                    if (shouldMangeMailboxForDmarc(m)) {
                        mangledMailboxes.add(m);
                        groupAddresses.set(j, mangleMailbox(m));
                    }
                }
            } else {
                throw new AssertionException();
            }
        }
        newMaildata.setFromAddresses(fromAddresses);

        if (list.isReplyToList()) {
            List<Address> ccAddresses = newMaildata.getCcAddresses();
            ccAddresses.addAll(mangledMailboxes);
        } else {
            List<Address> replyToAddresses = newMaildata.getReplyToAddresses();
            replyToAddresses.addAll(mangledMailboxes);
        }
    }

    private boolean shouldMangeMailboxForDmarc(Mailbox m)
            throws RejectExceptionExt {
        DomainPart remotePart = m.addrSpec.domain;
        if (remotePart instanceof LiteralDomainPart)
            return false;
        DotAtomDomainPart domain = (DotAtomDomainPart) remotePart;

        try {
            PolicyRecord discoverPolicy =
                    new PolicyDiscovery().discoverPolicy(domain);
            return discoverPolicy.request == Request.reject;
        } catch (RecoverableDmarcException e) {
            throw new RejectExceptionExt(new EnhancedStatus(451, "4.4.3",
                    "Unable to query for a DMARC record"));
        }
    }

    private Mailbox mangleMailbox(Mailbox m) {
        StringBuilder displayName = new StringBuilder();
        if (m.displayName != null) {
            displayName.append(m.displayName);
        } else {
            displayName.append(m.addrSpec.localPart);
        }
        displayName.append(" via ").append(list.getShortListName());
        Mailbox result = new Mailbox();
        result.displayName = displayName.toString();
        result.addrSpec = getListAddrSpec();
        return result;
    }

    private void sendMail() throws RejectExceptionExt {
        Mail mail = new Mail();
        mail.from = list.reversePath;
        for (ListMember member : list.members) {
            if (member.isDisabled() || member.isNoDelivery())
                continue;
            mail.recipients.add(member.getRecipient());
        }
        if (mail.recipients.isEmpty()) {
            logger.debug("Mail list has no such members, "
                    + "who should receive mail, dropping mail");
            return;
        }

        mail.maildata = newMaildata;
        try {
            mail.arrivalDate = source.arrivalDate;
            mail.scheduleDate = mail.arrivalDate; // try to preserve order
            list.transmitter.transmit(mail);
            logger.debug("Mailing list mail was submitted to transmitter: {}",
                    mail);
        } catch (LocalMailSystemException e) {
            logger.error("Cannot transmit mail", e);
            throw new RejectExceptionExt(e.errorStatus());
        }
    }

    private AddrSpec getListAddrSpec() {
        try {
            return AddrSpec.fromString(list.getAddress());
        } catch (ParseException e) {
            throw new ConfigurationException(e);
        }
    }
}
