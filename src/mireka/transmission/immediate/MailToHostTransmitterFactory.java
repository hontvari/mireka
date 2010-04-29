package mireka.transmission.immediate;

import mireka.ClientFactory;
import mireka.transmission.queuing.LogIdFactory;

public class MailToHostTransmitterFactory {
    private ClientFactory clientFactory;
    private LogIdFactory logIdFactory;

    public MailToHostTransmitter create(RemoteMta remoteMta) {
        return new MailToHostTransmitter(clientFactory, logIdFactory, remoteMta);
    }

    /**
     * @category GETSET
     */
    public void setClientFactory(ClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    /**
     * @category GETSET
     */
    public void setLogIdFactory(LogIdFactory logIdFactory) {
        this.logIdFactory = logIdFactory;
    }

}
