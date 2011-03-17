package mireka.transmission.immediate;

import mireka.smtp.ClientFactory;
import mireka.transmission.queuing.LogIdFactory;

public class MailToHostTransmitterFactory {
    private ClientFactory clientFactory;
    private OutgoingConnectionsRegistry outgoingConnectionRegistry;
    private LogIdFactory logIdFactory;

    public MailToHostTransmitter create(RemoteMta remoteMta) {
        return new MailToHostTransmitter(clientFactory,
                outgoingConnectionRegistry, logIdFactory, remoteMta);
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
    public void setOutgoingConnectionRegistry(
            OutgoingConnectionsRegistry outgoingConnectionRegistry) {
        this.outgoingConnectionRegistry = outgoingConnectionRegistry;
    }

    /**
     * @category GETSET
     */
    public OutgoingConnectionsRegistry getOutgoingConnectionRegistry() {
        return outgoingConnectionRegistry;
    }

    /**
     * @category GETSET
     */
    public void setLogIdFactory(LogIdFactory logIdFactory) {
        this.logIdFactory = logIdFactory;
    }

}
