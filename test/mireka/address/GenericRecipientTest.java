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
}
