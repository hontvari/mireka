package mireka.transmission.queuing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import mireka.address.Recipient;
import mireka.address.RemotePart;
import mireka.address.RemotePartContainingRecipient;
import mireka.smtp.EnhancedStatus;
import mireka.transmission.Mail;
import mireka.transmission.Transmitter;
import mireka.transmission.immediate.ImmediateSender;
import mireka.transmission.queue.MailProcessor;
import mireka.transmission.queue.MailProcessorFactory;
import mireka.transmission.queue.QueueStorageException;
import mireka.transmission.queue.ScheduleFileDirQueue;
import mireka.transmission.queue.TransmitterSummary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueuingTransmitter implements Transmitter, MailProcessorFactory {
    private final Logger logger = LoggerFactory
            .getLogger(QueuingTransmitter.class);
    private ScheduleFileDirQueue queue;
    private ImmediateSender immediateSender;
    private RetryPolicy retryPolicy;
    private LogIdFactory logIdFactory;
    private TransmitterSummary summary;

    @Override
    public void transmit(Mail mail) throws QueueStorageException {
        logger.debug("Mail received for transmission: {}", mail);
        if (immediateSender.singleDomainOnly()) {
            queueByRemotePart(mail);
        } else {
            queue.add(mail);
            logger.debug("Mail was added to queue: {}", mail);
        }
    }

    private void queueByRemotePart(Mail mail) throws QueueStorageException {
        List<List<Recipient>> recipientsByDomain =
                groupRecipientsByDomain(mail.recipients);
        if (recipientsByDomain.isEmpty())
            throw new IllegalArgumentException("No recipients");
        for (List<Recipient> recipients : recipientsByDomain) {
            Mail mailToSingleDomain = mail.copy();
            mailToSingleDomain.recipients.clear();
            mailToSingleDomain.recipients.addAll(recipients);
            queue.add(mailToSingleDomain);
        }
        logger.debug("Mail addressed to {} domains was added to queue: {}",
                recipientsByDomain.size(), mail);
    }

    private List<List<Recipient>> groupRecipientsByDomain(
            List<Recipient> recipients) throws QueueStorageException {
        Map<RemotePart, List<Recipient>> map =
                new LinkedHashMap<RemotePart, List<Recipient>>();
        for (Recipient recipient : recipients) {
            if (recipient.isGlobalPostmaster())
                throw new QueueStorageException("System is incorrectly " +
                            "configured to send mail addressed to the global " +
                            "postmaster using a domain dependent sender method. " +
                            "(postmaster should be the alias of a local account)", 
                            EnhancedStatus.INCORRECT_CONFIGURATION);
            RemotePart remotePart =
                    ((RemotePartContainingRecipient) recipient).getMailbox()
                            .getRemotePart();
            List<Recipient> remotePartRecipientList = map.get(remotePart);
            if (remotePartRecipientList == null) {
                remotePartRecipientList = new ArrayList<Recipient>();
                map.put(remotePart, remotePartRecipientList);
            }
            remotePartRecipientList.add(recipient);
        }
        return new ArrayList<List<Recipient>>(map.values());
    }

    @Override
    public MailProcessor create(Mail mail) {
        return new OutboundMtaMailProcessor(immediateSender, retryPolicy,
                logIdFactory, summary, mail);
    }

    /**
     * configuration
     * 
     * @category GETSET
     */
    public void setQueue(ScheduleFileDirQueue queue) {
        this.queue = queue;
    }

    /**
     * @category GETSET
     */
    public void setImmediateSender(ImmediateSender immediateSender) {
        this.immediateSender = immediateSender;
    }

    /**
     * configuration
     * 
     * @category GETSET
     */
    public void setRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    /**
     * configuration
     * 
     * @category GETSET
     */
    public void setLogIdFactory(LogIdFactory logIdFactory) {
        this.logIdFactory = logIdFactory;
    }

    /**
     * @category GETSET
     */
    public void setSummary(TransmitterSummary summary) {
        this.summary = summary;
    }
}
