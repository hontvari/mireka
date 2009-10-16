package mireka.submission;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mireka.filter.MailTransaction;

public class ConnectedFromAuthorizedIpAddress implements
        MailTransactionSpecification {
    private final Logger logger =
            LoggerFactory.getLogger(ConnectedFromAuthorizedIpAddress.class);
    private final List<IpAddress> ipAddresses = new ArrayList<IpAddress>();

    public void addAddress(IpAddress ipAddress) {
        ipAddresses.add(ipAddress);
    }

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

}
