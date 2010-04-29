package mireka.address;

import static org.junit.Assert.*;

import org.junit.Test;

public class DomainTest {

    @Test
    public void testEqualsCaseInsensitive() {
        Domain domainUpper = new Domain("EXAMPLE.COM");
        Domain domainLower = new Domain("example.com");
        assertTrue(domainUpper.equals(domainLower));
    }

}
