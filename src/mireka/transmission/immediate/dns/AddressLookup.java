package mireka.transmission.immediate.dns;

import java.net.InetAddress;

import javax.annotation.concurrent.ThreadSafe;

import mireka.smtp.EnhancedStatus;
import mireka.smtp.SendException;

import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;

/**
 * The AddressLookup class queries the IP address of an MTA or domain by
 * querying the A and AAAA records assigned to the domain name of the MTA.
 * <p>
 * This implementation uses Dnsjava, therefore it can provide much more 
 * precise error messages than the InetAddress. It also respects DNS TTL
 * values. 
 */
@ThreadSafe
public class AddressLookup {

    public InetAddress[] queryAddresses(Name name) throws SendException {
        Record[] records = queryAddressRecords(name);
        InetAddress[] addresses = convertAddressRecordsToAddresses(records);
        return addresses;
    }

    private Record[] queryAddressRecords(Name name) throws SendException {
        Lookup lookup = new Lookup(name);
        Record[] records = lookup.run();
        switch (lookup.getResult()) {
        case Lookup.SUCCESSFUL:
            return records;
        case Lookup.TYPE_NOT_FOUND:
            throw new SendException("Host " + name + " has no address record ("
                    + lookup.getErrorString() + ")",
                    EnhancedStatus.PERMANENT_UNABLE_TO_ROUTE);
        case Lookup.HOST_NOT_FOUND:
            throw new SendException("Host " + name + " is not found ("
                    + lookup.getErrorString() + ")",
                    EnhancedStatus.PERMANENT_UNABLE_TO_ROUTE);
        case Lookup.TRY_AGAIN:
            throw new SendException(
                    "DNS network failure while looking up address of " + name
                            + ": " + lookup.getErrorString(),
                    EnhancedStatus.TRANSIENT_DIRECTORY_SERVER_FAILURE);
        case Lookup.UNRECOVERABLE:
            throw new SendException(
                    "Unrecoverable DNS error while looking up address of "
                            + name + ": " + lookup.getErrorString(),
                    EnhancedStatus.PERMANENT_UNABLE_TO_ROUTE);
        default:
            throw new SendException(
                    "Unknown DNS status while looking up address of " + name
                            + ": " + lookup.getResult() + ". "
                            + lookup.getErrorString(),
                    EnhancedStatus.PERMANENT_INTERNAL_ERROR);
        }
    }

    private InetAddress[] convertAddressRecordsToAddresses(Record[] records) {
        InetAddress[] addresses = new InetAddress[records.length];
        for (int i = 0; i < records.length; i++) {
            Record record = records[i];
            if (record instanceof ARecord) {
                addresses[i] = ((ARecord) record).getAddress();
            } else if (record instanceof AAAARecord) {
                addresses[i] = ((AAAARecord) record).getAddress();
            } else {
                throw new RuntimeException();
            }
        }
        return addresses;
    }

}
