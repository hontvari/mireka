package mireka.transmission.immediate.dns;

import static mireka.ExampleAddress.*;
import static org.junit.Assert.*;

import java.net.InetAddress;

import mireka.smtp.SendException;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;

import org.junit.Test;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;

public class AddressLookupTest {

    @Tested
    private AddressLookup addressLookup;

    @Mocked
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

        InetAddress[] addresses =
                addressLookup.queryAddresses(HOST1_EXAMPLE_COM_NAME);

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

        InetAddress[] addresses =
                addressLookup.queryAddresses(HOST1_EXAMPLE_COM_NAME);

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
            addressLookup.queryAddresses(HOST1_EXAMPLE_COM_NAME);
            fail("An exception must have been thrown.");
            return;
        } catch (SendException e1) {
            e = e1;
        }

        assertTrue(e.errorStatus().shouldRetry());
    }
}
