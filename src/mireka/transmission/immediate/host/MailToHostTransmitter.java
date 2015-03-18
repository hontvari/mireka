package mireka.transmission.immediate.host;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import mireka.address.Recipient;
import mireka.smtp.EnhancedStatus;
import mireka.smtp.SendException;
import mireka.smtp.client.MtaAddress;
import mireka.smtp.client.SmtpClient;
import mireka.transmission.Mail;
import mireka.transmission.immediate.PostponeException;
import mireka.transmission.immediate.RecipientRejection;
import mireka.transmission.immediate.RecipientsWereRejectedException;
import mireka.transmission.immediate.RemoteMtaErrorResponseException;
import mireka.transmission.queuing.LogIdFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.client.SMTPException;
import org.subethamail.smtp.client.SmartClient;

/**
 * MailToHostTransmitter transmits a mail to a specific host specified by its IP
 * address.
 */
public class MailToHostTransmitter {
    private final Logger logger = LoggerFactory
            .getLogger(MailToHostTransmitter.class);
    private OutgoingConnectionsRegistry outgoingConnectionRegistry;
    private LogIdFactory logIdFactory;

    /**
     * Delivers the mail to the SMTP server running on the specified host.
     * 
     * @param client
     *            an unconnected, but otherwise fully initialized
     *            {@link SmtpClient}.
     * @throws PostponeException
     *             if it has not even tried connecting to the host, because it
     *             is likely that the host is busy at this moment.
     */
    public void transmit(Mail mail, SmtpClient client) throws SendException,
            RecipientsWereRejectedException, PostponeException {
        MtaAddress remoteMta = client.getMtaAddress();
        try {
            outgoingConnectionRegistry
                    .openConnection(client.getMtaAddress().address);
        } catch (PostponeException e) {
            e.setRemoteMta(remoteMta);
            throw e;
        }
        try {
            client.connect();
            client.from(mail.from.getSmtpText());
            List<RecipientRejection> recipientRejections =
                    new ArrayList<RecipientRejection>();
            List<Recipient> acceptedRecipients = new ArrayList<Recipient>();
            for (Recipient recipient : mail.recipients) {
                try {
                    client.to(recipient.sourceRouteStripped());
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
            client.dataStart();
            writeMailConent(mail, client);
            client.dataEnd();
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
                            "No answer from host or bad connection"));
        } finally {
            if (client != null) {
                client.quit();
            }
            outgoingConnectionRegistry.releaseConnection(remoteMta.address);
        }
    }

    private void writeMailConent(Mail mail, SmartClient smartClient)
            throws IOException {
        SmartClientOutputStreamAdapter out =
                new SmartClientOutputStreamAdapter(smartClient);
        mail.mailData.writeTo(out);
    }

    /**
     * @x.category GETSET
     */
    public void setOutgoingConnectionRegistry(
            OutgoingConnectionsRegistry outgoingConnectionRegistry) {
        this.outgoingConnectionRegistry = outgoingConnectionRegistry;
    }

    /**
     * @x.category GETSET
     */
    public OutgoingConnectionsRegistry getOutgoingConnectionRegistry() {
        return outgoingConnectionRegistry;
    }

    /**
     * @x.category GETSET
     */
    public void setLogIdFactory(LogIdFactory logIdFactory) {
        this.logIdFactory = logIdFactory;
    }
}
