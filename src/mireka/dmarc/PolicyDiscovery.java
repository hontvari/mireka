package mireka.dmarc;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import mireka.maildata.DotAtomDomainPart;
import mireka.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

/**
 * PolicyDiscovery queries the DNS for the policy record and parses it.
 */
public class PolicyDiscovery {

    private final Logger logger = LoggerFactory
            .getLogger(PolicyDiscovery.class);
    private Name fromDomainName;

    /**
     * Returns the DMARC policy record which applies to the supplied From domain
     * part, or null if no DMARC record is published.
     */
    public PolicyRecord discoverPolicy(DotAtomDomainPart domainPart)
            throws RecoverableDmarcException {
        try {
            fromDomainName = Name.fromString(domainPart.domain, Name.root);
        } catch (TextParseException e) {
            logger.debug("Syntax error in domain name", e);
            return null;
        }

        Name dmarcDomain = nameFromConstantString("_dmarc", fromDomainName);
        Record[] records = queryTxtRecords(dmarcDomain);
        List<String> policyRecordStrings = convertTxtRecordsToStrings(records);
        PolicyRecord record = parsePolicyRecordStrings(policyRecordStrings);
        return record;
    }

    /**
     * Returns the single valid DMARC policy record, or null, if there is none,
     * or more than one.
     */
    private PolicyRecord parsePolicyRecordStrings(
            List<String> policyRecordStrings) {
        PolicyRecord record = null;
        for (String recordString : policyRecordStrings) {

            PolicyRecord currentRecord = null;
            try {
                currentRecord = new PolicyRecordParser().parse(recordString);
            } catch (ParseException e) {
                logger.debug("Cannot parse record {} of {}", recordString,
                        fromDomainName);
            }
            if (record == null) {
                record = currentRecord;
            } else if (currentRecord != null) {
                logger.debug("Two or more DMARC policy record found for {}",
                        fromDomainName);
                return null;
            }
        }
        return record;
    }

    private Record[] queryTxtRecords(Name name)
            throws RecoverableDmarcException {
        Lookup lookup = new Lookup(name, Type.TXT);
        Record[] records = lookup.run();
        checkForError(lookup);
        if (records == null)
            records = new Record[0];
        return records;
    }

    private List<String> convertTxtRecordsToStrings(Record[] records) {
        List<String> result = new ArrayList<>();
        for (Record record : records) {
            if (!(record instanceof TXTRecord))
                throw new RuntimeException("Assertion failed");
            TXTRecord r = (TXTRecord) record;
            String concatenatedStringValue = concatenateTxtRecordStrings(r);
            result.add(concatenatedStringValue);
        }
        return result;
    }

    private String concatenateTxtRecordStrings(TXTRecord r) {
        StringBuilder buffer = new StringBuilder();
        @SuppressWarnings("unchecked")
        List<byte[]> stringsAsByteArrays =
                (List<byte[]>) r.getStringsAsByteArrays();
        for (byte[] byteString : stringsAsByteArrays) {
            buffer.append(CharsetUtil.toAsciiCharacters(byteString));
        }
        return buffer.toString();
    }

    private void checkForError(Lookup lookup) throws RecoverableDmarcException {
        switch (lookup.getResult()) {
        case Lookup.SUCCESSFUL:
            return;
        case Lookup.HOST_NOT_FOUND:
            // no DMARC policy published, that is a valid result too.
            return;
        case Lookup.TRY_AGAIN:
            throw new RecoverableDmarcException(
                    "DNS lookup failed because of a temporary issue, "
                            + lookup.getResult() + ", "
                            + lookup.getErrorString());
        default:
            logger.debug("DNS lookup failed because of a permanent problem, "
                    + lookup.getResult() + ", " + lookup.getErrorString());
            return;
        }
    }

    private Name nameFromConstantString(String s, Name origin) {
        try {
            return Name.fromString("_dmarc", origin);
        } catch (TextParseException e) {
            throw new IllegalArgumentException("Not a valid domain label: " + s);
        }
    }

}
