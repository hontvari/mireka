package mireka.address;

import static org.junit.Assert.*;

import org.junit.Test;

public class DomainPostmasterTest {
    @Test
    public void testCaseInsensitiveLocalPart() {
        DomainPostmaster postmasterUpper =
                new DomainPostmaster("Postmaster@example.com");
        DomainPostmaster postmasterLower =
                new DomainPostmaster("postmaster@example.com");
        assertTrue(postmasterLower.equals(postmasterUpper));
    }
}
