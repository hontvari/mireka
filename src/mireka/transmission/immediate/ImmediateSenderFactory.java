package mireka.transmission.immediate;

import mireka.transmission.immediate.dns.AddressLookupFactory;
import mireka.transmission.immediate.dns.MxLookupFactory;

public class ImmediateSenderFactory {
    private MailToHostTransmitterFactory mailToHostTransmitterFactory;

    public ImmediateSender create() {
        return new ImmediateSender(new MxLookupFactory(),
                new AddressLookupFactory(), mailToHostTransmitterFactory);
    }

    /**
     * @category GETSET
     */
    public void setMailToHostTransmitterFactory(
            MailToHostTransmitterFactory mailToHostTransmitterFactory) {
        this.mailToHostTransmitterFactory = mailToHostTransmitterFactory;
    }
}
