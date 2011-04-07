package mireka.address.parser;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.text.ParseException;


import mireka.address.parser.ast.AddressLiteralRemotePartAST;
import mireka.address.parser.ast.Ipv4RemotePartAST;
import mireka.address.parser.ast.Ipv6RemotePartAST;
import mireka.address.parser.ast.MailboxAST;

import org.junit.Ignore;
import org.junit.Test;

public class MailboxParserTest {
    @Ignore("Requires manual testing, tests JRE")
    @Test
    public void testJREInetAddressParsingContactsDNS() throws Exception {
        InetAddress.getByName("[::192.0.2.0.2]");
    }

    @Test
    public void testMailboxLocalPartGood() throws Exception {
        parse("a@example.com");
        parse("john@example.com");
        parse("john.doe@example.com");
        parse("\"john doe\"@example.com"); // "john doe"@
        parse("\"john@doe\"@example.com"); // "john@doe"@
        parse("\"john\\\"doe\"@example.com"); // "john\"doe"@
    }

    @Test
    public void testMailboxRemotePartGood() throws Exception {
        parse("john@example-with-hyphen.com");
        parse("john@localhost");
        parse("john@1");
        parse("john@[192.0.2.0]");
        parse("john@[IPv6:2001:DB8::]");
    }

    @Test
    public void testMailboxBothPartBad() throws Exception {
        assertSyntaxError("");
        assertSyntaxError("@");
    }

    @Test
    public void testMailboxLocalPartBad() throws Exception {
        assertSyntaxError(".@example.com");
        assertSyntaxError("john..doe@example.com");
        assertSyntaxError("\"@example.com"); // "@
        assertSyntaxError("john doe@example.com");
        assertSyntaxError("john@doe@example.com");
    }

    @Test
    public void testMailboxRemotePartBad() throws Exception {
        assertSyntaxError("john@");
        assertSyntaxError("john@example-with-wrong-hyphen-.com");
        assertSyntaxError("john@example..com");
        assertSyntaxError("john@.com");
        assertSyntaxError("john@[IPv6:::]a");
        assertSyntaxError("john@[IPv6::::]");
    }

    @Test
    public void testCompleteSpelling() throws Exception {
        MailboxAST mailboxAST = parse("john@example.com");
        assertEquals("john@example.com", mailboxAST.spelling);
    }

    @Test
    public void testLocalPartSpelling() throws Exception {
        MailboxAST mailboxAST = parse("john@example.com");
        assertEquals("john", mailboxAST.localPartAST.spelling);
    }

    @Test
    public void testDomainSpelling() throws Exception {
        MailboxAST mailboxAST = parse("john@example.com");
        assertEquals("example.com", mailboxAST.remotePartAST.spelling);
    }

    @Test
    public void testIpv4Spelling() throws Exception {
        MailboxAST mailboxAST = parse("john@[192.0.2.0]");
        assertEquals("[192.0.2.0]", mailboxAST.remotePartAST.spelling);
    }

    @Test
    public void testIpv6Spelling() throws Exception {
        MailboxAST mailboxAST = parse("john@[IPv6:2001:DB8::]");
        assertEquals("[IPv6:2001:DB8::]", mailboxAST.remotePartAST.spelling);
    }

    @Test
    public void testIpv4Address() throws Exception {
        MailboxAST mailboxAST = parse("john@[192.0.2.0]");
        assertEquals(InetAddress.getByName("192.0.2.0"),
                ((Ipv4RemotePartAST) (mailboxAST.remotePartAST)).ipv4.address);
        assertEquals(InetAddress.getByName("192.0.2.0"),
                ((AddressLiteralRemotePartAST) (mailboxAST.remotePartAST)).address);
    }

    @Test
    public void testIpv6Address() throws Exception {
        MailboxAST mailboxAST = parse("john@[IPv6:2001:DB8::]");
        assertEquals(InetAddress.getByName("[2001:DB8::]"),
                ((Ipv6RemotePartAST) (mailboxAST.remotePartAST)).ipv6.address);
        assertEquals(InetAddress.getByName("[2001:DB8::]"),
                ((AddressLiteralRemotePartAST) (mailboxAST.remotePartAST)).address);
    }

    private MailboxAST parse(String address) throws ParseException {
        return new MailboxParser(address).parse();
    }

    private void assertSyntaxError(String address) {
        try {
            new MailboxParser(address).parse();
            fail("ParseException expected for " + address);
        } catch (ParseException e) {
            // good
        }
    }
}
