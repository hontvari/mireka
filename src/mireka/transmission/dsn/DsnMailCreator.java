package mireka.transmission.dsn;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;

import mireka.maildata.Maildata;
import mireka.maildata.io.TmpMaildataFile;
import mireka.smtp.EnhancedStatus;
import mireka.smtp.MailSystemStatus;
import mireka.smtp.address.MailAddressFactory;
import mireka.smtp.address.NullReversePath;
import mireka.smtp.address.Recipient;
import mireka.smtp.address.ReversePath;
import mireka.transmission.LocalMailSystemException;
import mireka.transmission.Mail;
import mireka.util.DateTimeRfc822Formatter;
import mireka.util.MultilineParser;

import org.apache.james.mime4j.codec.EncoderUtil;
import org.apache.james.mime4j.dom.BinaryBody;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.field.address.AddressBuilder;
import org.apache.james.mime4j.field.address.ParseException;
import org.apache.james.mime4j.message.BasicBodyFactory;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.DefaultMessageWriter;
import org.apache.james.mime4j.message.MessageImpl;
import org.apache.james.mime4j.message.MultipartImpl;
import org.apache.james.mime4j.util.MimeUtil;

/**
 * DsnMailCreator constructs a DSN message. It does not collect any status
 * information itself, its sole responsibility is to format the message based on
 * the supplied data.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc3464">RFC 3464 An Extensible
 *      Message Format for Delivery Status Notifications</a>
 */
@ThreadSafe
public class DsnMailCreator {
    private final DateTimeRfc822Formatter dateTimeRfc822Formatter =
            new DateTimeRfc822Formatter();
    /**
     * The DNS/HELO name of the MTA which attempts the transfer (i.e. this MTA).
     * It appears in the report.
     */
    private String reportingMtaName;
    /**
     * The address used in the From header of the DSN message. Something like
     * MAILER-DAEMON@example.com.
     */
    private NameAddr fromAddress;

    /**
     * Constructs a new DSN message.
     * 
     * @param mail
     *            the mail of which transmission status will be reported
     * @param recipientReports
     *            recipient specific information about the status
     */
    public Mail create(Mail mail, List<RecipientProblemReport> recipientReports)
            throws LocalMailSystemException {
        return new DsnMailCreatorInner(mail, recipientReports).create();
    }

    /**
     * Sets the DNS/HELO name of this MTA. It appears in the report.
     */
    public void setReportingMtaName(String reportingMtaName) {
        this.reportingMtaName = reportingMtaName;
    }

    /**
     * Sets the address used in the From header of the DSN mail.
     */
    public void setFromAddress(NameAddr fromAddress) {
        this.fromAddress = fromAddress;
    }

    private class DsnMailCreatorInner {
        private final Mail originalMail;
        private final List<RecipientProblemReport> recipientReports;
        private final Mail resultMail = new Mail();

        public DsnMailCreatorInner(Mail mail,
                List<RecipientProblemReport> recipientReports) {
            this.originalMail = mail;
            this.recipientReports = recipientReports;
        }

        public Mail create() throws LocalMailSystemException {
            setupEnvelope();
            setupMessageContent();
            return resultMail;
        }

        private void setupEnvelope() {
            resultMail.arrivalDate = new Date();
            resultMail.scheduleDate = resultMail.arrivalDate;
            resultMail.from = new NullReversePath();
            Recipient recipient;
            recipient =
                    new MailAddressFactory()
                            .reversePath2Recipient(originalMail.from);
            resultMail.recipients.add(recipient);
        }

        private void setupMessageContent() throws LocalMailSystemException {
            Message message = message();
            TmpMaildataFile maildataFile = new TmpMaildataFile();
            try {
                try (OutputStream out =
                        maildataFile.deferredFile.getOutputStream()) {
                    new DefaultMessageWriter().writeMessage(message, out);
                }
                message.dispose();

                resultMail.maildata = new Maildata(maildataFile);
            } catch (IOException e) {
                maildataFile.close();
                throw new LocalMailSystemException(e,
                        EnhancedStatus.TRANSIENT_LOCAL_ERROR_IN_PROCESSING);
            }
        }

        private Message message() {
            MessageImpl message = new MessageImpl();
            message.setDate(new Date());
            message.createMessageId(reportingMtaName);
            message.setSubject("Delivery Status Notification");
            message.setFrom(fromAddress.toMime4jMailbox());
            message.setTo(reversePathToMime4jMailbox(originalMail.from));

            Multipart report = multipartReport();
            message.setMultipart(report,
                    Collections.singletonMap("report-type", "delivery-status"));
            return message;
        }

        private Mailbox reversePathToMime4jMailbox(ReversePath reversePath) {
            try {
                return AddressBuilder.DEFAULT.parseMailbox(reversePath
                        .getSmtpText());
            } catch (ParseException e) {
                // impossible
                throw new RuntimeException(e);
            }
        }

        private Multipart multipartReport() {
            Multipart result = new MultipartImpl("report");
            result.addBodyPart(humanReadableTextBodyPart());
            result.addBodyPart(deliveryStatusBodyPart());
            result.addBodyPart(originalMessageBodyPart());
            return result;
        }

        private BodyPart humanReadableTextBodyPart() {
            BodyPart result = new BodyPart();
            TextBody textBody =
                    new BasicBodyFactory().textBody(humanReadableText());
            result.setText(textBody);
            return result;
        }

        private String humanReadableText() {
            StringBuilder buffer = new StringBuilder();
            buffer.append("This is an automatically generated "
                    + "Delivery Status Notification.");
            buffer.append("\r\n");
            buffer.append("\r\n");
            buffer.append("The original message was received at ");
            buffer.append(dateTimeRfc822Formatter
                    .format(originalMail.arrivalDate));
            buffer.append("\r\n");
            buffer.append("from ");
            buffer.append(originalMail.from);
            buffer.append("\r\n");
            buffer.append("\r\n");
            buffer.append("    ----- The following addresses had delivery problems -----");
            buffer.append("\r\n");
            for (RecipientProblemReport report : recipientReports) {
                buffer.append("<");
                buffer.append(report.recipient);
                buffer.append(">");
                buffer.append("  (");
                buffer.append(report.actionCode());
                buffer.append(")");
                buffer.append("\r\n");
            }
            return buffer.toString();
        }

        private BodyPart deliveryStatusBodyPart() {
            BodyPart result = new BodyPart();
            TextBody textBody =
                    new BasicBodyFactory().textBody(deliveryStatusText());
            result.setBody(textBody, "message/delivery-status");
            return result;
        }

        private String deliveryStatusText() {
            StringBuilder buffer = new StringBuilder();
            buffer.append(messageDsn());
            for (RecipientProblemReport recipientFailure : recipientReports) {
                buffer.append("\r\n");
                buffer.append(recipientDsnForRecipientProblemReport(recipientFailure));
            }
            return buffer.toString();
        }

        private String messageDsn() {
            HeaderPrinter result = new HeaderPrinter();
            result.add("Reporting-MTA", "dns; " + reportingMtaName);
            if (originalMail.receivedFromMtaAddress != null)
                result.add("Received-From-MTA", "dns; "
                        + receivedFromMtaString());
            result.add("Arrival-Date", originalMail.arrivalDate);
            return result.toString();
        }

        private String receivedFromMtaString() {
            InetAddress address = originalMail.receivedFromMtaAddress;
            String heloName = originalMail.receivedFromMtaName;
            StringBuilder buffer = new StringBuilder();
            if (heloName == null)
                buffer.append(address.getHostAddress());
            else
                buffer.append(heloName);
            buffer.append(" (");
            buffer.append(address.getHostName());
            buffer.append(" [");
            buffer.append(address.getHostAddress());
            buffer.append("])");
            return buffer.toString();
        }

        private String recipientDsnForRecipientProblemReport(
                RecipientProblemReport failure) {
            HeaderPrinter headers = new HeaderPrinter();
            headers.add("Final-Recipient",
                    "rfc822; " + failure.recipient.sourceRouteStripped());
            headers.add("Action", failure.actionCode());
            headers.add("Status", formattedStatus(failure.status));
            if (failure.remoteMtaDiagnosticStatus != null) {
                headers.add("Remote-MTA", "dns; " + failure.remoteMta.dnsName);
                if (!failure.status.equals(failure.remoteMtaDiagnosticStatus)) {
                    headers.add(
                            "Diagnostic-Code",
                            diagnosticCodeForRemoteMtaStatus(failure.remoteMtaDiagnosticStatus));
                }
            }
            headers.add("Last-Attempt-Date", failure.failureDate);
            headers.add("Final-Log-ID", failure.logId);
            return headers.toString();
        }

        private String formattedStatus(EnhancedStatus status) {
            return status.getEnhancedStatusCode() + " (" + status.getMessage()
                    + ")";
        }

        private String diagnosticCodeForRemoteMtaStatus(
                MailSystemStatus smtpStatus) {
            MultilineParser parser =
                    new MultilineParser(smtpStatus.getDiagnosticCode());
            StringBuilder buffer = new StringBuilder();
            while (parser.hasNext()) {
                String line = parser.next();
                if (!parser.atFirstLine())
                    buffer.append("\r\n ");
                buffer.append(line);
            }
            return "smtp; " + buffer.toString();
        }

        private BodyPart originalMessageBodyPart() {
            BinaryBody body = new MessageContentBody(originalMail.maildata);
            BodyPart result = new BodyPart();
            result.setBody(body, "message/rfc822");
            return result;
        }

    }

    private static class HeaderPrinter {
        private final StringBuilder buffer = new StringBuilder(256);

        public void add(String name, String value) {
            int usedCharacters = name.length() + 2;
            String encodedValue =
                    EncoderUtil.encodeIfNecessary(value,
                            EncoderUtil.Usage.TEXT_TOKEN, usedCharacters);
            String foldedField = MimeUtil.fold(name + ": " + encodedValue, 0);
            buffer.append(foldedField).append("\r\n");
        }

        public void add(String name, Date date) {
            String value = new DateTimeRfc822Formatter().format(date);
            add(name, value);
        }

        @Override
        public String toString() {
            return buffer.toString();
        }
    }

    private static class MessageContentBody extends BinaryBody {

        private final Maildata maildata;

        public MessageContentBody(Maildata maildata) {
            this.maildata = maildata;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return maildata.getInputStream();
        }

        @Override
        public void writeTo(OutputStream out) throws IOException {
            maildata.writeTo(out);
        }

        @Override
        public void dispose() {
            maildata.close();
        }
    }
}