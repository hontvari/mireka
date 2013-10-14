package mireka.transmission.immediate;

import java.util.Arrays;
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
 * IndirectImmediateSender synchronously sends all mails through other SMTP
 * servers specified in the configuration, typically to a smarthost, instead of
 * sending the mail directly to the SMTP servers of the recipients. It tries all
 * listed smarthosts until a working one is found. The smarthost will in turn
 * transmit the mail to remote domains. This is useful for example if a network
 * is behind a dynamic IP address, considering that dynamic IP addresses are
 * frequently rejected by SMTP servers.
 * <p>
 * If a smart host name resolves to more than one IP addresses, than only the
 * first one is used.
 * <p>
 * Instead of specifying a single smarthost, an Upstream with more servers can
 * also be supplied and Mireka will distribute outgoing mails like a simple load
 * balancer.
 */
@NotThreadSafe
public class IndirectImmediateSender implements ImmediateSender {
    private final Logger logger = LoggerFactory
            .getLogger(IndirectImmediateSender.class);
    private MailToHostTransmitter mailToHostTransmitter;
    private Upstream upstream = new Upstream();

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
        List<BackendServer> servers = upstream.orderedServerList();
        logger.debug("Trying backends in this order: {}", servers);
        for (BackendServer server : servers) {
            SmtpClient client;
            try {
                client = server.createClient();
            } catch (SendException e) {
                if (e.errorStatus().shouldRetry())
                    lastRetryableException = e;
                else
                    lastUnrecoverableDnsException = e;
                logger.debug("Looking up address of MTA {} failed, continuing "
                        + "with the next MTA if one is available: {}", server,
                        e.getMessage());
                continue;
            }

            try {
                mailToHostTransmitter.transmit(mail, client);
                return;
            } catch (PostponeException e) {
                lastPostponeException = e;
                logger.debug("Sending to SMTP host {} must be postponed, "
                        + "continuing with the next "
                        + "smart host if one is available: {}",
                        client.getMtaAddress(), e.getMessage());
            } catch (SendException e) {
                if (e.errorStatus().shouldRetry()) {
                    // lastSendException = e;
                    lastRetryableException = e;
                    logger.debug("Sending to SMTP host {} failed, "
                            + "continuing with the next "
                            + "smart host if one is available: {}",
                            client.getMtaAddress(), e.getMessage());
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

    /**
     * Sets the upstream to the supplied single server.
     * 
     * @category GETSET
     **/
    public void setBackendServer(BackendServer server) {
        upstream.setServers(Arrays.asList(server));
    }

    /** @category GETSET **/
    public Upstream getUpstream() {
        return upstream;
    }

    /** @category GETSET **/
    public void setUpstream(Upstream upstream) {
        this.upstream = upstream;
    }

}
