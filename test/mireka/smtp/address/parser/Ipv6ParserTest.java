package mireka.smtp.address.parser;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;

import mireka.smtp.address.parser.Ipv6Parser;
import mireka.smtp.address.parser.Ipv6Parser.Ipv6;
import mireka.smtp.address.parser.base.CharScanner;

import org.junit.Test;

public class Ipv6ParserTest {
    @Test
    public void testGoodAddresses() throws Exception {
        parse("0:0:0:0:0:0:0:0");

        parse("::");
        parse("1::");
        parse("::1");
        parse("1::1");

        parse("1:2::3:4");

        parse("1:2:3:4:5:6:192.0.2.0");
        parse("1:2::3:4:192.0.2.0");
        parse("::ffff:192.0.2.0");
        parse("::192.0.2.0");
        parse("1::192.0.2.0");

        parse("::0001");

        parse("a::");
    }

    @Test
    public void testBadAddress() throws Exception {
        assertSyntaxError("0");
        assertSyntaxError("::ffff:192.0.2");
        assertSyntaxError(":::");
        assertSyntaxError("1:2:3:4:5:6:7");
        assertSyntaxError("1:2:3:4:5:6:7:8:9");
        assertSyntaxError("1::2:3:4:5:6:7:8");
        assertSyntaxError("12345::");
        assertSyntaxError("::256.0.0.0");
        assertSyntaxError("::f.0.0.0");
    }

    @Test
    public void testSpelling() throws Exception {
        String address = "2001:DB8::";
        Ipv6 ipv6AST = new Ipv6Parser(new CharScanner(address)).parse();
        assertEquals(address, ipv6AST.spelling);
    }

    private void parse(String address) throws ParseException,
            UnknownHostException {
        Ipv6 ipv6AST = new Ipv6Parser(new CharScanner(address)).parse();
        assertEquals(InetAddress.getByName(address), ipv6AST.address);
    }

    private void assertSyntaxError(String address) {
        try {
            new Ipv6Parser(new CharScanner(address)).parse();
            fail("ParseException expected for " + address);
        } catch (ParseException e) {
            // good
        }
    }
}
