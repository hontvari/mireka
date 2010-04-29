package mireka.transmission.immediate.dns;

import java.net.InetAddress;

import mireka.transmission.EnhancedStatus;
import mireka.transmission.immediate.RemoteMta;
import mireka.transmission.immediate.SendException;

import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;

public class AddressLookup {
    private final Name name;
    private final RemoteMta remoteMta;

    public AddressLookup(Name name) {
        this.name = name;
        this.remoteMta = new RemoteMta(name.toString());
    }

    public InetAddress[] queryAddresses() throws SendException {
        Record[] records = queryAddressRecords();
        InetAddress[] addresses = convertAddressRecordsToAddresses(records);
        return addresses;
    }

    private Record[] queryAddressRecords() throws SendException {
        Lookup lookup = new Lookup(name);
        Record[] records = lookup.run();
        switch (lookup.getResult()) {
        case Lookup.SUCCESSFUL:
            return records;
        case Lookup.TYPE_NOT_FOUND:
            throw new SendException("Host " + name + " has no address record ("
                    + lookup.getErrorString() + ")",
                    EnhancedStatus.PERMANENT_UNABLE_TO_ROUTE, remoteMta);
        case Lookup.HOST_NOT_FOUND:
            throw new SendException("Host " + name + " is not found ("
                    + lookup.getErrorString() + ")",
                    EnhancedStatus.PERMANENT_UNABLE_TO_ROUTE, remoteMta);
        case Lookup.TRY_AGAIN:
            throw new SendException(
                    "DNS network failure while looking up address of " + name
                            + ": " + lookup.getErrorString(),
                    EnhancedStatus.TRANSIENT_DIRECTORY_SERVER_FAILURE,
                    remoteMta);
        case Lookup.UNRECOVERABLE:
            throw new SendException(
                    "Unrecoverable DNS error while looking up address of "
                            + name + ": " + lookup.getErrorString(),
                    EnhancedStatus.PERMANENT_UNABLE_TO_ROUTE, remoteMta);
        default:
            throw new SendException(
                    "Unknown DNS status while looking up address of " + name
                            + ": " + lookup.getResult() + ". "
                            + lookup.getErrorString(),
                    EnhancedStatus.PERMANENT_INTERNAL_ERROR, remoteMta);
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
