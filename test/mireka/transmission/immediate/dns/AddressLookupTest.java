package mireka.transmission.immediate.dns;

import static mireka.ExampleAddress.*;
import static org.junit.Assert.*;

import java.net.InetAddress;

import mireka.transmission.immediate.SendException;
import mockit.Expectations;
import mockit.NonStrict;

import org.junit.Test;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;

public class AddressLookupTest {

    private AddressLookup addressLookup = new AddressLookup(
            HOST1_EXAMPLE_COM_NAME);
    @NonStrict
    private Lookup lookup;

    @Test
    public void testQueryAddresses() throws SendException {
        new Expectations() {
            {
                lookup.run();
                result =
                        new Record[] {
                                new ARecord(HOST1_EXAMPLE_COM_NAME, 0, 0, IP1),
                                new ARecord(HOST1_EXAMPLE_COM_NAME, 0, 0, IP2)

                        };

            }
        };

        InetAddress[] addresses = addressLookup.queryAddresses();

        InetAddress[] expected = new InetAddress[] { IP1, IP2 };
        assertArrayEquals(expected, addresses);
    }

    @Test
    public void testQueryAddressesIpv6() throws SendException {
        new Expectations() {
            {
                lookup.run();
                result =
                        new Record[] { new AAAARecord(HOST6_EXAMPLE_COM_NAME,
                                0, 0, IPV6)

                        };

            }
        };

        InetAddress[] addresses = addressLookup.queryAddresses();

        InetAddress[] expected = new InetAddress[] { IPV6 };
        assertArrayEquals(expected, addresses);
    }

    @Test
    public void testTransientDnsFailure() {
        new Expectations() {
            {
                lookup.run();
                result = null;

                lookup.getResult();
                result = Lookup.TRY_AGAIN;
            }
        };

        SendException e;
        try {
            addressLookup.queryAddresses();
            fail("An exception must have been thrown.");
            return;
        } catch (SendException e1) {
            e = e1;
        }

        assertTrue(e.errorStatus().shouldRetry());
    }
}
