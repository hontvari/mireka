package mireka.filter.dnsbl;

import java.net.InetAddress;
import java.net.UnknownHostException;

import mireka.smtp.SmtpReplyTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Address;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

/**
 * Dnsbl represents a single domain based blackhole list. It can query the list
 * if an IP address is listed and if it is, the reason of the listing.
 */
public class Dnsbl {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String EOL = System.getProperty("line.separator");
    public String domain;
    public SmtpReplyTemplate smtpReplyTemplate = new SmtpReplyTemplate();

    public DnsblResult check(InetAddress address) {
        return new Checker(address).check();
    }

    @Override
    public String toString() {
        return domain.toString();
    }

    /**
     * @x.category GETSET
     */
    public String getDomain() {
        return domain;
    }

    /**
     * @x.category GETSET
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * @x.category GETSET
     */
    public SmtpReplyTemplate getSmtpReply() {
        return smtpReplyTemplate;
    }

    /**
     * @x.category GETSET
     */
    public void setSmtpReply(SmtpReplyTemplate smtpReplyTemplate) {
        this.smtpReplyTemplate = smtpReplyTemplate;
    }

    private class Checker {
        private final InetAddress address;
        private String queryDomain;

        public Checker(InetAddress address) {
            this.address = address;
        }

        public DnsblResult check() {
            queryDomain = reversedOctets(address) + "." + domain;
            InetAddress replyAddress = queryARecord();
            if (replyAddress == null) {
                logger.debug(
                        "DNSBL checked: NOT LISTED, dnsbl={}, address: {}",
                        domain, address);
                return DnsblResult.NOT_LISTED;
            }
            String reason = queryReason();
            DnsblResult result =
                    new DnsblResult(Dnsbl.this, replyAddress, reason);
            logger.debug("DNSBL checked: {}; address: {}", result, address);
            return result;
        }

        private String reversedOctets(InetAddress address) {
            StringBuilder buffer = new StringBuilder();
            for (byte octet : address.getAddress()) {
                int octetInt = octet & 0xFF;
                if (buffer.length() != 0)
                    buffer.insert(0, '.');
                buffer.insert(0, octetInt);
            }
            return buffer.toString();
        }

        /**
         * @return null if the host is not listed
         */
        private InetAddress queryARecord() {
            try {
                return Address.getByName(queryDomain);
            } catch (UnknownHostException e) {
                return null;
            }
        }

        private String queryReason() {
            Lookup lookup;
            try {
                lookup = new Lookup(queryDomain, Type.TXT);
            } catch (TextParseException e) {
                throw new RuntimeException(e);
            }
            Record[] records = lookup.run();
            if (lookup.getResult() != Lookup.SUCCESSFUL
                    && lookup.getResult() != Lookup.TYPE_NOT_FOUND) {
                logger.warn("Error while looking up TXT record for "
                        + "address {} in DNSBL {}: {}", new Object[] { address,
                        domain, lookup.getErrorString() });
            }
            String reason = concatenateTxtRecordValues(records);
            return reason;
        }

        private String concatenateTxtRecordValues(Record[] records) {
            if (records == null || records.length == 0)
                return null;
            StringBuilder builder = new StringBuilder();
            for (Record record : records) {
                TXTRecord txtRecord = (TXTRecord) record;
                if (builder.length() != 0)
                    builder.append(EOL);
                for (Object string : txtRecord.getStrings()) {
                    if (builder.length() != 0)
                        builder.append(EOL);
                    builder.append(string);
                }
            }
            return builder.toString();
        }

    }
}
