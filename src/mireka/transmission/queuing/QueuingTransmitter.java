package mireka.transmission.queuing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import mireka.address.Recipient;
import mireka.address.RemotePart;
import mireka.address.RemotePartContainingRecipient;
import mireka.transmission.Mail;
import mireka.transmission.Transmitter;
import mireka.transmission.immediate.ImmediateSenderFactory;
import mireka.transmission.queue.MailProcessor;
import mireka.transmission.queue.MailProcessorFactory;
import mireka.transmission.queue.QueueStorageException;
import mireka.transmission.queue.ScheduleFileDirQueue;
import mireka.transmission.queue.TransmitterSummary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueuingTransmitter implements Transmitter, MailProcessorFactory {
    private Logger logger = LoggerFactory.getLogger(QueuingTransmitter.class);
    private ScheduleFileDirQueue queue;
    private ImmediateSenderFactory immediateSenderFactory;
    private RetryPolicy retryPolicy;
    private LogIdFactory logIdFactory;
    private TransmitterSummary summary;

    public void transmit(Mail mail) throws QueueStorageException {
        logger.debug("Mail received for transmission: {}", mail);
        queueByRemotePart(mail);
    }

    private void queueByRemotePart(Mail mail) throws QueueStorageException {
        List<List<Recipient>> recipientsByDomain =
                groupRecipientsByDomain(mail.recipients);
        if (recipientsByDomain.isEmpty())
            throw new IllegalArgumentException("No recipients");
        for (List<Recipient> recipients : recipientsByDomain) {
            mail.recipients = recipients;
            queue.add(mail);
        }
        logger.debug("Mail addressed to {} domains was added to queue: {}",
                recipientsByDomain.size(), mail);
    }

    private List<List<Recipient>> groupRecipientsByDomain(
            List<Recipient> recipients) {
        Map<RemotePart, List<Recipient>> map =
                new LinkedHashMap<RemotePart, List<Recipient>>();
        for (Recipient recipient : recipients) {
            RemotePart remotePart =
                    ((RemotePartContainingRecipient) recipient).getAddress()
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
        return new OutboundMtaMailProcessor(immediateSenderFactory,
                retryPolicy, logIdFactory, summary, mail);
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
    public void setImmediateSenderFactory(
            ImmediateSenderFactory immediateSenderFactory) {
        this.immediateSenderFactory = immediateSenderFactory;
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
