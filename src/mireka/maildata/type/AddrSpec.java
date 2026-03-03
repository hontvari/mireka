package mireka.maildata.type;

import java.text.ParseException;

import mireka.maildata.parser.FieldGenerator;
import mireka.maildata.parser.StructuredFieldBodyParser;

public class AddrSpec {
    public String spelling;

    public LocalPart localPart;
    /**
     * Either a {@link DotAtomDomainPart} or a {@link LiteralDomainPart}.
     */
    public DomainPart domain;

    public String generate() {
        FieldGenerator g = new FieldGenerator();
        g.writeAddrSpec(this);
        return g.toString();
    }

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
