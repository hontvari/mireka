package mireka.transmission.immediate.dns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import mireka.address.Domain;
import mireka.smtp.EnhancedStatus;
import mireka.transmission.immediate.SendException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;

/**
 * This class wraps DNS query functionality in order to make unit testing
 * easier.
 */
@NotThreadSafe
public class MxLookup {
    private static MXRecordPriorityComparator mxRecordPriorityComparator =
            new MXRecordPriorityComparator();
    private final Logger logger = LoggerFactory.getLogger(MxLookup.class);
    private final Domain domain;

    public MxLookup(Domain domain) {
        this.domain = domain;
    }

    /**
     * Returns an ordered host name list based on the MX records of the domain.
     * The first has the highest priority. If the domain has no MX records, then
     * it returns the host itself. Records with the same priority are shuffled
     * randomly.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc5321#section-5.1">RFC 5321
     *      Simple Mail Transfer Protocol - Locating the Target Host</a>
     */
    public Name[] queryMxTargets() throws MxLookupException {
        MXRecord[] records = queryMxRecords();
        if (records.length == 0) {
            logger.debug("Domain {} has no MX records, using an implicit "
                    + "MX record targetting the host", domain);
            return implicitMxTargetForDomain();
        } else {
            //
            ArrayList<MXRecord> list =
                    new ArrayList<MXRecord>(Arrays.asList(records));
            Collections.shuffle(list);
            // This sort is guaranteed to be stable: equal elements will not be
            // reordered as a result of the sort, so shuffle remains in effect
            Collections.sort(list, mxRecordPriorityComparator);
            list.toArray(records);
            return convertMxRecordsToHostNames(records);
        }
    }

    /**
     * looks up MX records in the DNS system
     * 
     * @param domain
     * @return an empty array if no MX record was found
     * @throws SendException
     *             if the domain is invalid or if the DNS lookup fails because
     *             the domain is not registered or the DNS servers are not
     *             accessible or any other DNS related problem
     */
    private MXRecord[] queryMxRecords() throws MxLookupException {
        org.xbill.DNS.Lookup lookup;

        try {
            lookup = new Lookup(domain.smtpText(), org.xbill.DNS.Type.MX);
        } catch (org.xbill.DNS.TextParseException e) {
            throw new MxLookupException(e,
                    EnhancedStatus.BAD_DESTINATION_MAILBOX_ADDRESS_SYNTAX);
        }

        Record[] recordsGeneric = lookup.run();
        MXRecord[] records = null;
        if (recordsGeneric != null) {
            records = new MXRecord[recordsGeneric.length];
            for (int i = 0; i < recordsGeneric.length; i++)
                records[i] = (MXRecord) recordsGeneric[i];
        }
        int errorCode = lookup.getResult();

        if (errorCode == Lookup.UNRECOVERABLE)
            throw new MxLookupException(domain + " " + lookup.getErrorString(),
                    EnhancedStatus.PERMANENT_UNABLE_TO_ROUTE);
        else if (errorCode == Lookup.TRY_AGAIN)
            throw new MxLookupException(domain + " " + lookup.getErrorString(),
                    EnhancedStatus.TRANSIENT_DIRECTORY_SERVER_FAILURE);
        else if (errorCode == Lookup.HOST_NOT_FOUND)
            throw new MxLookupException(domain + " " + lookup.getErrorString(),
                    EnhancedStatus.BAD_DESTINATION_SYSTEM_ADDRESS);
        else if (errorCode == Lookup.SUCCESSFUL
                || errorCode == Lookup.TYPE_NOT_FOUND)
            ; // continue
        else
            throw new MxLookupException("Unknown DNS status: " + errorCode
                    + ". " + domain + " " + lookup.getErrorString(),
                    EnhancedStatus.PERMANENT_INTERNAL_ERROR);
        if (records == null) {
            return new MXRecord[0];
        }

        return records;
    }

    private Name[] implicitMxTargetForDomain() {
        Name[] singletonNames = new Name[] { domain.toName() };
        return singletonNames;
    }

    private Name[] convertMxRecordsToHostNames(MXRecord[] records) {
        Name[] result = new Name[records.length];
        for (int i = result.length; --i >= 0;)
            result[i] = records[i].getTarget();
        return result;
    }

    /**
     * compares MX records based on their priority value
     */
    @Immutable
    private static class MXRecordPriorityComparator implements
            java.util.Comparator<MXRecord> {
        public int compare(MXRecord o1, MXRecord o2) {
            int p1 = o1.getPriority();
            int p2 = o2.getPriority();
            if (p1 < p2)
                return -1;
            else if (p1 > p2)
                return +1;
            else
                return 0;
        }
    }

}
