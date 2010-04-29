package mireka.address;

import static org.junit.Assert.*;

import org.junit.Test;

public class GenericRecipientTest {

    @Test
    public void testSourceRouteStripped() {
        GenericRecipient recipient =
                new GenericRecipient(
                        "@example.net,@example.org:jane@example.com");
        assertEquals("jane@example.com", recipient.sourceRouteStripped());
    }

    @Test
    public void testAddressSourceRouteStripped() {
        GenericRecipient recipientWithForwardPath =
                new GenericRecipient(
                        "@example.net,@example.org:jane@example.com");
        GenericRecipient recipientWithoutForwardPath =
                new GenericRecipient("jane@example.com");
        assertTrue(recipientWithoutForwardPath.getAddress().equals(
                recipientWithForwardPath.getAddress()));
    }

}
