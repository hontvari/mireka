package mireka.maildata;

import java.text.ParseException;

import mireka.maildata.parser.StructuredFieldBodyParser;

public class AddrSpec {
    /**
     * The semantic content of the local part. For example if it was specified
     * as a quoted-string the mail header field, then quotes are not included in
     * this string.
     */
    public String localPart;
    /**
     * Either a {@link DotAtomDomainPart} or a {@link LiteralDomainPart}.
     */
    public DomainPart domain;

    /**
     * Creates a new instance from an addr-spec string.
     * 
     * @param address
     *            the string containing the address as it it specified in a mail
     *            header, e.g. <code>"Jon Postel"@example.org</code> or
     *            <code>jonathan@example.org</code>.
     */
    public static AddrSpec fromString(String address) throws ParseException {
        return new StructuredFieldBodyParser(address).parseAddrSpec();
    }
}
