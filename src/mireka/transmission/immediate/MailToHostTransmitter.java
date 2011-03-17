package mireka.transmission.immediate;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import mireka.address.Recipient;
import mireka.smtp.ClientFactory;
import mireka.smtp.EnhancedStatus;
import mireka.transmission.Mail;
import mireka.transmission.queuing.LogIdFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.client.SMTPException;
import org.subethamail.smtp.client.SmartClient;

/**
 * MailToHostTransmitter transmits a mail to a specific host specified by its IP
 * address.
 */
@NotThreadSafe
class MailToHostTransmitter {
    private final Logger logger = LoggerFactory
            .getLogger(MailToHostTransmitter.class);
    private final ClientFactory clientFactory;
    private OutgoingConnectionsRegistry outgoingConnectionsRegistry;
    private final LogIdFactory logIdFactory;
    private final RemoteMta remoteMta;
    private Mail mail;

    public MailToHostTransmitter(ClientFactory clientFactory,
            OutgoingConnectionsRegistry outgoingConnectionsRegistry,
            LogIdFactory logIdFactory, RemoteMta remoteMta) {
        this.clientFactory = clientFactory;
        this.outgoingConnectionsRegistry = outgoingConnectionsRegistry;
        this.logIdFactory = logIdFactory;
        this.remoteMta = remoteMta;

    }

    /**
     * Delivers the mail to the SMTP server running on the specified host.
     * 
     * @param inetAddress
     *            The receiving SMTP host. The name (if there is one) must
     *            already be resolved (using dnsJava).
     * @throws PostponeException
     *             if it has not even tried connecting to the host, because it
     *             is likely that the host is busy at this moment.
     */
    public void transmit(Mail mail, InetAddress inetAddress)
            throws SendException, RecipientsWereRejectedException,
            PostponeException {
        this.mail = mail;
        SmartClient smartClient = null;
        try {
            outgoingConnectionsRegistry.openConnection(inetAddress);
        } catch (PostponeException e) {
            e.setRemoteMta(remoteMta);
            throw e;
        }
        try {
            smartClient = clientFactory.create(inetAddress);
            smartClient.from(mail.from);
            List<RecipientRejection> recipientRejections =
                    new ArrayList<RecipientRejection>();
            List<Recipient> acceptedRecipients = new ArrayList<Recipient>();
            for (Recipient recipient : mail.recipients) {
                try {
                    smartClient.to(recipient.sourceRouteStripped());
                    acceptedRecipients.add(recipient);
                } catch (SMTPException e) {
                    RemoteMtaErrorResponseException sendException =
                            new RemoteMtaErrorResponseException(e, remoteMta);
                    recipientRejections.add(new RecipientRejection(recipient,
                            sendException));
                    String logId = logIdFactory.next();
                    sendException.initLogId(logId);
                    logger.debug("Recipient " + recipient
                            + " was rejected/failed. Log-ID=" + logId
                            + ". Continuing with the next recipient if one "
                            + "exists. " + e.getResponse());
                }
            }
            if (acceptedRecipients.isEmpty()) {
                logger.debug("All recipients were rejected");
                throw new RecipientsWereRejectedException(recipientRejections);
            }
            smartClient.dataStart();
            writeDataTo(smartClient);
            smartClient.dataEnd();
            if (!recipientRejections.isEmpty())
                throw new RecipientsWereRejectedException(recipientRejections);
            else
                return;
        } catch (SMTPException e) {
            throw new RemoteMtaErrorResponseException(e, remoteMta);
        } catch (UnknownHostException e) {
            // impossible
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new SendException("Connection failed: " + e.toString(), e,
                    new EnhancedStatus(450, "4.4.0",
                            "No answer from host or bad connection"), remoteMta);
        } finally {
            if (smartClient != null) {
                smartClient.quit();
            }
            outgoingConnectionsRegistry.releaseConnection(inetAddress);
        }
    }

    private void writeDataTo(SmartClient smartClient) throws IOException {
        SmartClientOutputStreamAdapter out =
                new SmartClientOutputStreamAdapter(smartClient);
        mail.mailData.writeTo(out);
    }
}
