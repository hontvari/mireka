package mireka.smtp.client;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import javax.annotation.concurrent.Immutable;

import org.xbill.DNS.Name;

/**
 * MtaAddress contains all informations which are necessary to contact that MTA
 * and identify it in DSN reports, logs.
 */
@Immutable
public class MtaAddress {
    /**
     * The host name as an RFC-5321 domain name or address-literal. It comes
     * from the content of a DNS MX record or an email address with a literal
     * address part, or a smart host or back-end server name. For example:
     * <ul>
     * <li>mail.example.com
     * <li>[192.0.2.0]
     * <li>[IPv6:::1]
     * </ul>
     */
    public final String dnsName;

    /**
     * IP address of MTA. It is possible that a domain name in an MX records
     * have multiple A records, therefore a single domain name may refer to
     * multiple MTA servers, and only this address differentiates between them.
     */
    public final InetAddress address;

    public final int port;

    /**
     * @param smtpFormattedHost
     *            The host name as an RFC-5321 domain name or address-literal.
     *            It comes from the content of a DNS MX record or an email
     *            address with a literal address part, or a smart host or
     *            back-end server name. For example:
     *            <ul>
     *            <li>mail.example.com
     *            <li>[192.0.2.0]
     *            <li>[IPv6:::1]
     *            </ul>
     * @param address
     *            IP address of the MTA
     * @param port
     *            port of the remote MTA
     */
    public MtaAddress(String smtpFormattedHost, InetAddress address, int port) {
        this.dnsName = smtpFormattedHost;
        this.address = address;
        this.port = port;
    }

    /**
     * Constructs a new MtaAddress with the default port, which is 25. This is
     * the equivalent of calling {@link #MtaAddress(String, InetAddress, int)}
     * with port 25.
     */
    public MtaAddress(String dnsName, InetAddress address) {
        this(dnsName, address, 25);
    }

    /**
     * Constructs a new MtaAddress with the default port, which is 25. This is
     * the equivalent of calling {@link #MtaAddress(String, InetAddress, int)}
     * with port 25, and with a dnsName converted from the supplied DnsJava Name
     * object by removing the last dot. The trailing dot marks an absolute
     * domain name in DNS - but that syntax is invalid in SMTP.
     */
    public MtaAddress(Name dnsName, InetAddress address) {
        this(name2string(dnsName), address);
    }

    private static String name2string(Name name) {
        String s = name.toString();
        if (s.charAt(s.length() - 1) == '.')
            s = s.substring(0, s.length() - 1);
        return s;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        result = prime * result + ((dnsName == null) ? 0 : dnsName.hashCode());
        result = prime * result + port;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MtaAddress other = (MtaAddress) obj;
        if (address == null) {
            if (other.address != null)
                return false;
        } else if (!address.equals(other.address))
            return false;
        if (dnsName == null) {
            if (other.dnsName != null)
                return false;
        } else if (!dnsName.equals(other.dnsName))
            return false;
        if (port != other.port)
            return false;
        return true;
    }

    /**
     * Prints the address in a compact form for informational purposes.
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder(64);
        buffer.append(dnsName);

        if (address != null) {
            buffer.append(" (");
            if (address instanceof Inet4Address)
                buffer.append(address.getHostAddress());
            else if (address instanceof Inet6Address)
                buffer.append('[').append(address.getHostAddress()).append(']');
            else
                throw new RuntimeException("Assertion failed");
            buffer.append(':').append(port).append(')');
        }

        return buffer.toString();
    }

}