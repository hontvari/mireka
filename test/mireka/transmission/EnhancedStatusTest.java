package mireka.transmission;

import static org.junit.Assert.*;

import mireka.smtp.EnhancedStatus;
import mireka.transmission.immediate.Rfc821Status;

import org.junit.Test;
import org.subethamail.smtp.client.SMTPClient.Response;

public class EnhancedStatusTest {

    @Test
    public void testEnhancedStatusRfc821Status() {
        Response response = new Response(550, "mailbox unavailable");
        Rfc821Status rfc821Status = new Rfc821Status(response);
        EnhancedStatus enhancedStatus = new EnhancedStatus(rfc821Status);
        assertTrue(enhancedStatus.getEnhancedStatusCode().startsWith("5."));
        assertFalse(enhancedStatus.shouldRetry());
    }

    @Test
    public void testGetMessagePrefixedWithEnhancedStatusCodeEmpty() {
        EnhancedStatus status = new EnhancedStatus(500, "5.0.0", "");
        assertEquals("5.0.0", status.getMessagePrefixedWithEnhancedStatusCode());
    }

    @Test
    public void testGetMessagePrefixedWithEnhancedStatusCodeOneLine() {
        EnhancedStatus status =
                new EnhancedStatus(500, "5.0.0", "Example message");
        assertEquals("5.0.0 Example message", status
                .getMessagePrefixedWithEnhancedStatusCode());
    }

    @Test
    public void testGetMessagePrefixedWithEnhancedStatusCodeTwoLines() {
        EnhancedStatus status =
                new EnhancedStatus(500, "5.0.0",
                        "Example line 1\r\nExample line 2");
        assertEquals("5.0.0 Example line 1\r\n5.0.0 Example line 2", status
                .getMessagePrefixedWithEnhancedStatusCode());
    }
}
