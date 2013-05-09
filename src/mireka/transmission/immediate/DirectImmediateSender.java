package mireka.transmission.immediate;

import java.net.InetAddress;

import mireka.address.AddressLiteral;
import mireka.address.Domain;
import mireka.address.DomainPart;
import mireka.address.Recipient;
import mireka.address.RemotePart;
import mireka.address.RemotePartContainingRecipient;
import mireka.smtp.SendException;
import mireka.smtp.client.ClientFactory;
import mireka.smtp.client.MtaAddress;
import mireka.smtp.client.SmtpClient;
import mireka.transmission.Mail;
import mireka.transmission.immediate.dns.AddressLookup;
import mireka.transmission.immediate.dns.MxLookup;
import mireka.transmission.immediate.host.MailToHostTransmitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Name;

/**
 * DirectImmediateSender synchronously sends a mail directly to an SMTP server
 * of a single remote domain, which may include attempting delivery to more than
 * one MX hosts of the domain until a working one is found.
 * <p>
 * The remote domain is specified by the remote part of the recipient addresses,
 * which must be the same for all recipients in case of this implementation.
 * <p>
 * The receiving SMTP servers are usually specified by the MX records of the
 * remote domain, except if the remote part is a literal address, or the domain
 * has an implicit MX record only.
 * <p>
 * If it cannot transmit the mail to any of the MX hosts of the domain, then it
 * throws an exception, it does not retry later.
 * <p>
 * TODO: if a recipient is rejected because of a transient failure, then it
 * should be retried on another host.
 */
public class DirectImmediateSender implements ImmediateSender {
    private final Logger logger = LoggerFactory
            .getLogger(DirectImmediateSender.class);
    private MxLookup mxLookup;
    private AddressLookup addressLookup;
    private ClientFactory clientFactory;
    private MailToHostTransmitter mailToHostTransmitter;

    public DirectImmediateSender() {
        mxLookup = new MxLookup();
        addressLookup = new AddressLookup();
    }
    
    @Override
    public boolean singleDomainOnly() {
        return true;
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
    @Override
    public void send(Mail mail) throws SendException,
            RecipientsWereRejectedException, IllegalArgumentException,
            PostponeException {
        RemotePart remotePart = commonRecipientRemotePart(mail);
        if (remotePart instanceof AddressLiteral) {
            AddressLiteral addressLiteral = (AddressLiteral) remotePart;
            sendToAddressLiteral(mail, addressLiteral);
        } else if (remotePart instanceof DomainPart) {
            Domain domain = ((DomainPart) remotePart).domain;
            sendToDomain(mail, domain);
        } else {
            throw new RuntimeException();
        }
    }

    private RemotePart commonRecipientRemotePart(Mail mail)
            throws IllegalArgumentException {
        RemotePart result = null;
        for (Recipient recipient : mail.recipients) {
            if (!(recipient instanceof RemotePartContainingRecipient))
                throw new IllegalArgumentException(
                        "Cannot send mail to non-remote address: " + recipient);
            RemotePart remotePart =
                    ((RemotePartContainingRecipient) recipient).getMailbox()
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

    private void sendToAddressLiteral(Mail mail, AddressLiteral target)
            throws SendException, RecipientsWereRejectedException,
            PostponeException {
        MtaAddress mtaAddress =
                new MtaAddress(target.smtpText(), target.inetAddress());

        SmtpClient client = clientFactory.create();
        client.setMtaAddress(mtaAddress);

        mailToHostTransmitter.transmit(mail, client);
    }

    /**
     * Queries MX hosts of the domain and tries to transmit to the hosts until
     * it is successful or no more hosts remain.
     * 
     * @throws PostponeException
     *             if transmission to all of the hosts must be postponed,
     *             because all of them are assumed to be busy at this moment.
     */
    private void sendToDomain(Mail mail, Domain domain) throws SendException,
            RecipientsWereRejectedException, PostponeException {
        Name[] mxNames = mxLookup.queryMxTargets(domain);

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
                addresses = addressLookup.queryAddresses(name);
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
                    MtaAddress mtaAddress = new MtaAddress(name, hostAddress);
                    SmtpClient client = clientFactory.create();
                    client.setMtaAddress(mtaAddress);
                    mailToHostTransmitter.transmit(mail, client);
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

    /** @category GETSET **/
    public MxLookup getMxLookup() {
        return mxLookup;
    }

    /** @category GETSET **/
    public void setMxLookup(MxLookup mxLookup) {
        this.mxLookup = mxLookup;
    }

    /** @category GETSET **/
    public AddressLookup getAddressLookup() {
        return addressLookup;
    }

    /** @category GETSET **/
    public void setAddressLookup(
            AddressLookup addressLookup) {
        this.addressLookup = addressLookup;
    }

    /** @category GETSET **/
    public ClientFactory getClientFactory() {
        return clientFactory;
    }

    /** @category GETSET **/
    public void setClientFactory(ClientFactory clientFactory) {
        this.clientFactory = clientFactory;
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
}
