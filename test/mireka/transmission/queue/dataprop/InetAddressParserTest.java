package mireka.transmission.queue.dataprop;

import static org.junit.Assert.*;

import java.net.InetAddress;

import mireka.ExampleAddress;

import org.junit.Test;

public class InetAddressParserTest {

    @Test
    public void testParseFullySpecifiedAddress() {
        InetAddress originalAddress = ExampleAddress.IP;
        InetAddress parsedAddress =
                new InetAddressParser(originalAddress.toString()).parse();
        assertEquals(originalAddress, parsedAddress); // only checks address
        assertEquals(originalAddress.getHostName(), parsedAddress.getHostName());
    }

    @Test
    public void testParseOnlyAddressSpecified() {
        InetAddress originalAddress = ExampleAddress.IP_ADDRESS_ONLY;
        InetAddress parsedAddress =
                new InetAddressParser(originalAddress.toString()).parse();
        assertEquals(originalAddress, parsedAddress); // only checks address
    }
}
