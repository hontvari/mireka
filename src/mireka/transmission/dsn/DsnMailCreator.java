package mireka.transmission.dsn;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;
import javax.mail.internet.ParseException;

import mireka.MailData;
import mireka.address.MailAddressFactory;
import mireka.address.Recipient;
import mireka.transmission.EnhancedStatus;
import mireka.transmission.Mail;
import mireka.transmission.MailSystemStatus;
import mireka.util.DateTimeRfc822Formatter;
import mireka.util.MultilineParser;

import org.apache.james.mime4j.codec.EncoderUtil;
import org.apache.james.mime4j.field.DefaultFieldParser;
import org.apache.james.mime4j.field.address.Mailbox;
import org.apache.james.mime4j.message.BinaryBody;
import org.apache.james.mime4j.message.BodyFactory;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.Header;
import org.apache.james.mime4j.message.Message;
import org.apache.james.mime4j.message.Multipart;
import org.apache.james.mime4j.message.TextBody;
import org.apache.james.mime4j.parser.Field;
import org.apache.james.mime4j.util.ByteSequence;
import org.apache.james.mime4j.util.ContentUtil;
import org.apache.james.mime4j.util.MimeUtil;

@ThreadSafe
public class DsnMailCreator {
    private final DateTimeRfc822Formatter dateTimeRfc822Formatter =
            new DateTimeRfc822Formatter();
    /**
     * the DNS/HELO name of the MTA which attempted the transfer ( i.e. this
     * MTA. It appears in the report.
     */
    private final String reportingMtaName;
    private final NameAddr fromAddress;

    public DsnMailCreator(String reportingMtaName, NameAddr fromAddress) {
        this.reportingMtaName = reportingMtaName;
        this.fromAddress = fromAddress;
    }

    public Mail create(Mail mail, List<PermanentFailureReport> recipientStatuses) {
        return new DsnMailCreatorInner(mail, recipientStatuses).create();
    }

    private class DsnMailCreatorInner {
        private final Mime4jFieldFactory mime4jFieldFactory =
                new Mime4jFieldFactory();
        private final Mail originalMail;
        private final List<PermanentFailureReport> recipientStatuses;
        private final Mail resultMail = new Mail();

        public DsnMailCreatorInner(Mail mail,
                List<PermanentFailureReport> recipientStatuses) {
            this.originalMail = mail;
            this.recipientStatuses = recipientStatuses;
        }

        public Mail create() {
            setupEnvelope();
            setupMessageContent();
            return resultMail;
        }

        private void setupEnvelope() {
            resultMail.arrivalDate = new Date();
            resultMail.scheduleDate = resultMail.arrivalDate;
            resultMail.from = "";
            Recipient recipient;
            try {
                recipient =
                        new MailAddressFactory()
                                .createRecipient(originalMail.from);
            } catch (ParseException e) {
                // it mustn't happen at this time
                throw new IllegalArgumentException(
                        "Cannot create DSN mail, original reverse-path has "
                                + "syntax error.", e);
            }
            resultMail.recipients.add(recipient);
        }

        private void setupMessageContent() {
            Message message = message();
            resultMail.mailData = new Mime4jMessageMessageContent(message);
        }

        private Message message() {
            Message message = new Message();
            Mime4jHeaderBuilder headerBuilder =
                    new Mime4jHeaderBuilder(mime4jFieldFactory);
            headerBuilder.add("MIME-Version", "1.0");
            headerBuilder.add("Date", new Date());
            message.setHeader(headerBuilder.toHeader());

            message.createMessageId(reportingMtaName);
            message.setSubject("Delivery Status Notification");
            message.setFrom(fromAddress.toMime4jMailbox());
            message.setTo(Mailbox.parse(originalMail.from));

            Multipart report = multipartReport();
            message.setMultipart(report, Collections.singletonMap(
                    "report-type", "delivery-status"));
            return message;
        }

        private Multipart multipartReport() {
            Multipart result = new Multipart("report");
            result.addBodyPart(humanReadableTextBodyPart());
            result.addBodyPart(deliveryStatusBodyPart());
            result.addBodyPart(originalMessageBodyPart());
            return result;
        }

        private BodyPart humanReadableTextBodyPart() {
            BodyPart result = new BodyPart();
            TextBody textBody = new BodyFactory().textBody(humanReadableText());
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
            buffer
                    .append("    ----- The following addresses had delivery problems -----");
            buffer.append("\r\n");
            for (PermanentFailureReport failure : recipientStatuses) {
                buffer.append("<");
                buffer.append(failure.recipient);
                buffer.append(">");
                buffer.append("  (");
                buffer.append("unrecoverable error");
                buffer.append(")");
                buffer.append("\r\n");
            }
            return buffer.toString();
        }

        private BodyPart deliveryStatusBodyPart() {
            BodyPart result = new BodyPart();
            TextBody textBody =
                    new BodyFactory().textBody(deliveryStatusText());
            result.setBody(textBody, "message/delivery-status");
            return result;
        }

        private String deliveryStatusText() {
            StringBuilder buffer = new StringBuilder();
            buffer.append(messageDsn());
            for (PermanentFailureReport recipientFailure : recipientStatuses) {
                buffer.append("\r\n");
                buffer
                        .append(recipientDsnForPermanentFailure(recipientFailure));
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

        private String recipientDsnForPermanentFailure(
                PermanentFailureReport failure) {
            HeaderPrinter headers = new HeaderPrinter();
            headers.add("Final-Recipient", "rfc822; "
                    + failure.recipient.sourceRouteStripped());
            headers.add("Action", "failed");
            headers.add("Status", formattedStatus(failure.status));
            if (failure.remoteMtaDiagnosticStatus != null) {
                headers.add("Remote-MTA", "dns; " + failure.remoteMta.dnsName);
                if (!failure.status.equals(failure.remoteMtaDiagnosticStatus)) {
                    headers
                            .add(
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
            BinaryBody body = new MessageContentBody(originalMail.mailData);
            BodyPart result = new BodyPart();
            result.setBody(body, "message/rfc822");
            return result;
        }

    }

    private static class Mime4jMessageMessageContent implements MailData {
        private final Message message;

        public Mime4jMessageMessageContent(Message message) {
            this.message = message;
        }

        @Override
        public void writeTo(OutputStream out) throws IOException {
            message.writeTo(out);
        }

        @Override
        public void dispose() {
            message.dispose();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            // not used
            throw new UnsupportedOperationException();
        }
    }

    private static class Mime4jFieldFactory {
        private final DefaultFieldParser mime4jFieldParser =
                new DefaultFieldParser();

        private Field create(String name, String value) {
            int usedCharacters = name.length() + 2;
            String fieldValue =
                    EncoderUtil.encodeIfNecessary(value,
                            EncoderUtil.Usage.TEXT_TOKEN, usedCharacters);
            String rawStr = MimeUtil.fold(name + ": " + fieldValue, 0);
            ByteSequence raw = ContentUtil.encode(rawStr);
            return mime4jFieldParser.parse(name, fieldValue, raw);
        }
    }

    private static class Mime4jHeaderBuilder {
        private final Mime4jFieldFactory mime4jFieldFactory;
        private final Header header = new Header();

        public Mime4jHeaderBuilder(Mime4jFieldFactory mime4jFieldFactory) {
            this.mime4jFieldFactory = mime4jFieldFactory;
        }

        public void add(String name, String value) {
            header.addField(mime4jFieldFactory.create(name, value));
        }

        public void add(String name, Date date) {
            String value = new DateTimeRfc822Formatter().format(date);
            header.addField(mime4jFieldFactory.create(name, value));
        }

        public Header toHeader() {
            return header;
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

        private final MailData messageContent;

        public MessageContentBody(MailData messageContent) {
            this.messageContent = messageContent;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return messageContent.getInputStream();
        }

        @Override
        public void writeTo(OutputStream out) throws IOException {
            messageContent.writeTo(out);
        }
    }
}