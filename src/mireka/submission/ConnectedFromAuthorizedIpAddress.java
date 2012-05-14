package mireka.submission;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import mireka.filter.MailTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectedFromAuthorizedIpAddress implements
        MailTransactionSpecification {
    private final Logger logger = LoggerFactory
            .getLogger(ConnectedFromAuthorizedIpAddress.class);
    private final List<IpAddress> ipAddresses = new ArrayList<IpAddress>();

    @Override
    public boolean isSatisfiedBy(MailTransaction mailTransaction) {
        InetAddress clientAddress = mailTransaction.getRemoteInetAddress();
        for (IpAddress addessSpecification : ipAddresses) {
            if (addessSpecification.isSatisfiedBy(clientAddress)) {
                logger.debug("IP address is authorized, matches {}",
                        addessSpecification);
                return true;
            }
        }
        return false;
    }

    public void addAddress(IpAddress ipAddress) {
        ipAddresses.add(ipAddress);
    }

    public void setAddresses(List<IpAddress> addresses) {
        this.ipAddresses.clear();
        this.ipAddresses.addAll(addresses);
    }

}
