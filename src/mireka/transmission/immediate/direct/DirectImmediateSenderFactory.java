package mireka.transmission.immediate.direct;

import mireka.transmission.immediate.ImmediateSender;
import mireka.transmission.immediate.ImmediateSenderFactory;
import mireka.transmission.immediate.dns.AddressLookupFactory;
import mireka.transmission.immediate.dns.MxLookupFactory;
import mireka.transmission.immediate.host.MailToHostTransmitterFactory;

/**
 * DirectImmediateSenderFactory is an ImmediateSenderFactory which creates 
 * DirectImmediateSender instances.
 */
public class DirectImmediateSenderFactory implements ImmediateSenderFactory {
    private MailToHostTransmitterFactory mailToHostTransmitterFactory;

    @Override
    public ImmediateSender create() {
        return new DirectImmediateSender(new MxLookupFactory(),
                new AddressLookupFactory(), mailToHostTransmitterFactory);
    }

    /**
     * @category GETSET
     */
    public void setMailToHostTransmitterFactory(
            MailToHostTransmitterFactory mailToHostTransmitterFactory) {
        this.mailToHostTransmitterFactory = mailToHostTransmitterFactory;
    }

    @Override
    public boolean singleDomainOnly() {
        return true;
    }
}
