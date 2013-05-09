package mireka.transmission.immediate;

import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import mireka.smtp.SendException;
import mireka.smtp.client.BackendServer;
import mireka.smtp.client.SmtpClient;
import mireka.transmission.Mail;
import mireka.transmission.immediate.host.MailToHostTransmitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NullClientImmediateSender synchronously sends a mail to a smart host, trying
 * all listed smart hosts until a working one is found. The smart host will in
 * turn transmit the mail to remote domains. This is useful for example if a
 * network is behind a dynamic IP address, considering that dynamic IP addresses
 * are frequently rejected by SMTP servers.
 * <p>
 * If a smart host name resolves to more than one IP addresses, than only the
 * first one is used.
 */
@NotThreadSafe
public class NullClientImmediateSender implements ImmediateSender {
    private final Logger logger = LoggerFactory
            .getLogger(NullClientImmediateSender.class);
    private MailToHostTransmitter mailToHostTransmitter;
    private List<BackendServer> smartHosts;

    @Override
    public boolean singleDomainOnly() {
        return false;
    }

    /**
     * Transmits mail to a smart host.
     * 
     * @throws PostponeException
     *             if transmission to all of the hosts must be postponed,
     *             because all of them are assumed to be busy at this moment.
     */
    @Override
    public void send(Mail mail) throws SendException,
            RecipientsWereRejectedException, IllegalArgumentException,
            PostponeException {

        // a PostponeException does not prevent successful delivery using
        // another host, but it must be saved so if there are no more hosts then
        // this exception instance will be rethrown.
        PostponeException lastPostponeException = null;
        // if there is a host which failed, but which should be retried later,
        // then a following unrecoverable DNS exception on another host may
        // not prevent delivery, so this temporary exception will be returned
        SendException lastRetryableException = null;
        // an unrecoverable DNS exception may not prevent delivery (to another
        // host), so the function will continue, but it must be
        // saved, because maybe there is no more host.
        SendException lastUnrecoverableDnsException = null;
        for (BackendServer smartHost : smartHosts) {
            SmtpClient client;
            try {
                client = smartHost.createClient();
            } catch (SendException e) {
                if (e.errorStatus().shouldRetry())
                    lastRetryableException = e;
                else
                    lastUnrecoverableDnsException = e;
                logger.debug(
                        "Looking up address of MTA " + smartHost.toString()
                                + " failed, continuing with the next MTA "
                                + "if one is available: ", e.getMessage());
                continue;
            }

            try {
                mailToHostTransmitter.transmit(mail, client);
                return;
            } catch (PostponeException e) {
                lastPostponeException = e;
                logger.debug("Sending to SMTP host " + client.getMtaAddress()
                        + " must be postponed, continuing with the next "
                        + "smart host if one is available: " + e.getMessage());
            } catch (SendException e) {
                if (e.errorStatus().shouldRetry()) {
                    // lastSendException = e;
                    lastRetryableException = e;
                    logger.debug(
                            "Sending to SMTP host " + client.getMtaAddress()
                                    + " failed, continuing with the next "
                                    + "smart host if one is available: ",
                            e.getMessage());
                } else {
                    throw e;
                }
            }
        }

        // at this point it is known that the transmission was not successful

        if (lastRetryableException != null)
            throw lastRetryableException;
        if (lastPostponeException != null) {
            // there is at least one host successfully found in DNS but have not
            // tried
            throw lastPostponeException;
        }
        if (lastUnrecoverableDnsException == null)
            throw new RuntimeException(); // impossible, but prevents warning
        // an unrecoverable DNS exception
        throw lastUnrecoverableDnsException;
    }

    /** @category GETSET **/
    public MailToHostTransmitter getMailToHostTransmitter() {
        return mailToHostTransmitter;
    }

    /** @category GETSET **/
    public void setMailToHostTransmitter(
            MailToHostTransmitter mailToHostTransmitter) {
        this.mailToHostTransmitter = mailToHostTransmitter;
    }

    /** @category GETSET **/
    public List<BackendServer> getSmartHosts() {
        return smartHosts;
    }

    /** @category GETSET **/
    public void setSmartHosts(List<BackendServer> smartHosts) {
        this.smartHosts = smartHosts;
    }
}
