package mireka.address.parser;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;

import mireka.address.parser.Ipv4Parser.Ipv4;
import mireka.address.parser.base.CharScanner;

import org.junit.Test;

public class Ipv4ParserTest {
    @Test
    public void testGoodAddresses() throws Exception {
        parse("192.0.2.0");
    }

    @Test
    public void testBadAddress() throws Exception {
        assertSyntaxError("192.0.2");
        assertSyntaxError("192.0.2.0.1");
        assertSyntaxError("1920.0.2.0");
    }

    @Test
    public void testSpelling() throws Exception {
        String address = "192.0.2.0";
        Ipv4 ipv4AST = new Ipv4Parser(new CharScanner(address)).parse();
        assertEquals(address, ipv4AST.spelling);
    }

    private void parse(String address) throws ParseException,
            UnknownHostException {
        Ipv4 ipv4AST = new Ipv4Parser(new CharScanner(address)).parse();
        assertEquals(InetAddress.getByName(address), ipv4AST.address);
    }

    private void assertSyntaxError(String address) {
        try {
            new Ipv4Parser(new CharScanner(address)).parse();
            fail("ParseException expected for " + address);
        } catch (ParseException e) {
            // good
        }
    }

}
