package mireka.transmission.dsn;

import static org.junit.Assert.*;

import org.apache.james.mime4j.field.address.Mailbox;
import org.junit.Test;

public class NameAddrTest {

    @Test
    public final void testToMime4jMailbox() {
        NameAddr nameAddr = new NameAddr("John", "john@example.com");
        Mailbox mailbox = nameAddr.toMime4jMailbox();
        assertEquals("John", mailbox.getName());
        assertEquals("john", mailbox.getLocalPart());
        assertEquals("example.com", mailbox.getDomain());
    }
}
