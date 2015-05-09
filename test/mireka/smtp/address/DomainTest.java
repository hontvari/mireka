package mireka.smtp.address;

import static org.junit.Assert.assertTrue;
import mireka.smtp.address.Domain;

import org.junit.Test;

public class DomainTest {

    @Test
    public void testEqualsCaseInsensitive() {
        Domain domainUpper = new Domain("EXAMPLE.COM");
        Domain domainLower = new Domain("example.com");
        assertTrue(domainUpper.equals(domainLower));
    }

}
