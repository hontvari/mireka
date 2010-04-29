package mireka.transmission.immediate.dns;

import static org.junit.Assert.*;
import mireka.address.Domain;
import mireka.transmission.immediate.dns.MxLookup;
import mireka.transmission.immediate.dns.MxLookupException;

import org.junit.Test;

public class DnsJavaLookupTest {
    private static final String INVALID_EXAMPLE_DOMAIN_NAME =
            ". an invalid. example.net";

    @Test()
    public void testExceptionMessageContainsDomain() {
        MxLookup mxLookup =
                new MxLookup(new Domain(INVALID_EXAMPLE_DOMAIN_NAME));
        try {
            mxLookup.queryMxTargets();
        } catch (MxLookupException e) {
            assertTrue(e.getMessage().contains(INVALID_EXAMPLE_DOMAIN_NAME));
            return;
        }
        fail("An exception should have been thrown");
    }

}
