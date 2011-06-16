package mireka.transmission.immediate;

import java.net.InetAddress;

import javax.annotation.concurrent.NotThreadSafe;

import mireka.address.AddressLiteral;
import mireka.address.Domain;
import mireka.address.DomainPart;
import mireka.address.Recipient;
import mireka.address.RemotePart;
import mireka.address.RemotePartContainingRecipient;
import mireka.transmission.Mail;
import mireka.transmission.immediate.dns.AddressLookupFactory;
import mireka.transmission.immediate.dns.MxLookupFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Name;

/**
 * ImmediateSender sends a mail to a domain, which may include attempting
 * delivery to more than one MX hosts of the domain until a working one is
 * found. If it cannot transmit the mail to any of the MX hosts of the domain,
 * then it throws an exception, it does not retry later.
 * <p>
 * TODO: if a recipient is rejected because of a transient failure, then it
 * should be retried on another host.
 */
@NotThreadSafe
public class ImmediateSender {
    private final Logger logger = LoggerFactory
            .getLogger(ImmediateSender.class);
    private final MxLookupFactory mxLookupFactory;
    private final AddressLookupFactory addressLookupFactory;
    private final MailToHostTransmitterFactory mailToHostTransmitterFactory;
    private Mail mail;

    ImmediateSender(MxLookupFactory mxLookupFactory,
            AddressLookupFactory addressLookupFactory,
            MailToHostTransmitterFactory mailToHostTransmitterFactory) {
        this.mxLookupFactory = mxLookupFactory;
        this.addressLookupFactory = addressLookupFactory;
        this.mailToHostTransmitterFactory = mailToHostTransmitterFactory;
    }

    /**
     * Transmits mail to a single domain.
     * 
     * @throws IllegalArgumentException
     *             if the domains of the recipients are not the same, or if the
     *             recipient is the special global postmaster address, which has
     *             no absolute domain.
     * @throws PostponeException
     *             if transmission to all of the hosts must be postponed,
     *             because all of them are assumed to be busy at this moment.
     */
    public void send(Mail mail) throws SendException,
            RecipientsWereRejectedException, IllegalArgumentException,
            PostponeException {
        this.mail = mail;
        RemotePart remotePart = commonRecipientRemotePart();
        if (remotePart instanceof AddressLiteral) {
            AddressLiteral addressLiteral = (AddressLiteral) remotePart;
            sendToAddressLiteral(addressLiteral);
        } else if (remotePart instanceof DomainPart) {
            Domain domain = ((DomainPart) remotePart).domain;
            sendToDomain(domain);
        } else {
            throw new RuntimeException();
        }
    }

    private RemotePart commonRecipientRemotePart()
            throws IllegalArgumentException {
        RemotePart result = null;
        for (Recipient recipient : mail.recipients) {
            if (!(recipient instanceof RemotePartContainingRecipient))
                throw new IllegalArgumentException(
                        "Cannot send mail to non-remote address: " + recipient);
            RemotePart remotePart =
                    ((RemotePartContainingRecipient) recipient).getAddress()
                            .getRemotePart();
            if (result == null) {
                result = remotePart;
            } else {
                if (!result.equals(remotePart))
                    throw new IllegalArgumentException(
                            "Recipients are expected to belong to the same domain. "
                                    + " Recipient list contains both " + result
                                    + " and " + remotePart);
            }
        }
        if (result == null)
            throw new IllegalArgumentException("recipient list is empty");
        return result;
    }

    private void sendToAddressLiteral(AddressLiteral target)
            throws SendException, RecipientsWereRejectedException,
            PostponeException {
        RemoteMta remoteMta =
                new RemoteMta(target.toString(), target.inetAddress()
                        .getHostAddress());
        mailToHostTransmitterFactory.create(remoteMta).transmit(mail,
                target.inetAddress());
    }

    /**
     * Queries MX hosts of the domain and tries to transmit to the hosts until
     * it is successful or no more hosts remain.
     * 
     * @throws PostponeException
     *             if transmission to all of the hosts must be postponed,
     *             because all of them are assumed to be busy at this moment.
     */
    private void sendToDomain(Domain domain) throws SendException,
            RecipientsWereRejectedException, PostponeException {
        Name[] mxNames = mxLookupFactory.create(domain).queryMxTargets();

        // a PostponeException does not prevent successful delivery using
        // another host, but it must be saved so if there are no more hosts then
        // this exception instance will be rethrown.
        PostponeException lastPostponeException = null;
        // if there is a host which failed, but which should be retried later,
        // then a following unrecoverable DNS exception on another MX host may
        // not prevent delivery, so this temporary exception will be returned
        SendException lastRetryableException = null;
        // an unrecoverable DNS exception may not prevent delivery (to another
        // MX host of the domain), so the function will continue, but it must be
        // saved, because maybe there is no more host.
        SendException lastUnrecoverableDnsException = null;
        for (Name name : mxNames) {
            InetAddress[] addresses;
            try {
                addresses = addressLookupFactory.create(name).queryAddresses();
            } catch (SendException e) {
                if (e.errorStatus().shouldRetry())
                    lastRetryableException = e;
                else
                    lastUnrecoverableDnsException = e;
                logger.debug("Looking up address of MX host " + name
                        + " failed, continuing with the next MX host "
                        + "if one is available: ", e.getMessage());
                continue;
            }

            try {
                for (InetAddress hostAddress : addresses) {
                    RemoteMta remoteMta =
                            new RemoteMta(name.toString(),
                                    hostAddress.getHostAddress());
                    mailToHostTransmitterFactory.create(remoteMta).transmit(
                            mail, hostAddress);
                    return;
                }
            } catch (PostponeException e) {
                lastPostponeException = e;
                logger.debug("Sending to SMTP host " + name
                        + " must be postponed, continuing with the next "
                        + "MX host if one is available: " + e.getMessage());
            } catch (SendException e) {
                if (e.errorStatus().shouldRetry()) {
                    // lastSendException = e;
                    lastRetryableException = e;
                    logger.debug("Sending to SMTP host " + name
                            + " failed, continuing with the next "
                            + "MX host if one is available: ", e.getMessage());
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
}
