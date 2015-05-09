package mireka.transmission.immediate.dns;

import static org.junit.Assert.*;
import mireka.smtp.address.Domain;

import org.junit.Test;

public class DnsJavaLookupTest {
    private static final String INVALID_EXAMPLE_DOMAIN_NAME =
            ". an invalid. example.net";

    @Test()
    public void testExceptionMessageContainsDomain() {
        MxLookup mxLookup = new MxLookup();
        try {
            mxLookup.queryMxTargets(new Domain(INVALID_EXAMPLE_DOMAIN_NAME));
        } catch (MxLookupException e) {
            assertTrue(e.getMessage().contains(INVALID_EXAMPLE_DOMAIN_NAME));
            return;
        }
        fail("An exception should have been thrown");
    }

}
