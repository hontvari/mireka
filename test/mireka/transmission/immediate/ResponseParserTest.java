package mireka.transmission.immediate;

import static org.junit.Assert.*;
import mireka.transmission.EnhancedStatus;
import mireka.transmission.MailSystemStatus;

import org.junit.Test;
import org.subethamail.smtp.client.SMTPClient.Response;

public class ResponseParserTest {
    private static final int CODE500 = 500;
    private ResponseParser statusFactory = new ResponseParser();

    @Test
    public void testOneLineNoEnhanced() {
        Response response = new Response(CODE500, "Example error");

        MailSystemStatus mailSystemStatus =
                statusFactory
                        .createResponseLookingForEnhancedStatusCode(response);

        assertEquals(Rfc821Status.class, mailSystemStatus.getClass());
        assertEquals(CODE500, mailSystemStatus.getSmtpReplyCode());
        assertEquals("Example error", mailSystemStatus.getMessage());
    }

    @Test
    public void testMultiLineNoEnhanced() {
        Response response =
                new Response(CODE500, "Example error\r\nSecond line");

        MailSystemStatus mailSystemStatus =
                statusFactory
                        .createResponseLookingForEnhancedStatusCode(response);

        assertEquals(Rfc821Status.class, mailSystemStatus.getClass());
        assertEquals(CODE500, mailSystemStatus.getSmtpReplyCode());
        assertEquals("Example error\r\nSecond line", mailSystemStatus
                .getMessage());
    }

    @Test
    public void testSingleLineEnhanced() {
        Response response = new Response(CODE500, "5.0.0 Example error");

        MailSystemStatus mailSystemStatus =
                statusFactory
                        .createResponseLookingForEnhancedStatusCode(response);

        assertEquals(EnhancedStatus.class, mailSystemStatus.getClass());
        assertEquals(CODE500, mailSystemStatus.getSmtpReplyCode());
        assertEquals("Example error", mailSystemStatus.getMessage());
        assertEquals("5.0.0", ((EnhancedStatus) mailSystemStatus)
                .getEnhancedStatusCode());
    }

    @Test
    public void testMultiLineEnhanced() {
        Response response =
                new Response(CODE500,
                        "5.0.0 Example error\r\n5.0.0 Second line");

        MailSystemStatus mailSystemStatus =
                statusFactory
                        .createResponseLookingForEnhancedStatusCode(response);

        assertEquals(EnhancedStatus.class, mailSystemStatus.getClass());
        assertEquals(CODE500, mailSystemStatus.getSmtpReplyCode());
        assertEquals("Example error\r\nSecond line", mailSystemStatus
                .getMessage());
        assertEquals("5.0.0", ((EnhancedStatus) mailSystemStatus)
                .getEnhancedStatusCode());
    }

    @Test
    public void testMultigitEnhancedCode() {
        Response response = new Response(CODE500, "5.10.100 Example error");

        MailSystemStatus mailSystemStatus =
                statusFactory
                        .createResponseLookingForEnhancedStatusCode(response);

        assertEquals("5.10.100", ((EnhancedStatus) mailSystemStatus)
                .getEnhancedStatusCode());
    }

    @Test
    public void testEnhancedCodeInvalidBecause0Padded() {
        Response response = new Response(CODE500, "5.01.0 Example error");

        MailSystemStatus mailSystemStatus =
                statusFactory
                        .createResponseLookingForEnhancedStatusCode(response);

        assertEquals(Rfc821Status.class, mailSystemStatus.getClass());
        assertEquals("5.01.0 Example error", mailSystemStatus.getMessage());
    }

    @Test
    public void testEnhancedCodeInconsistent() {
        Response response =
                new Response(CODE500, "5.0.0 error\r\nMissing code!");

        MailSystemStatus mailSystemStatus =
                statusFactory
                        .createResponseLookingForEnhancedStatusCode(response);

        assertEquals(Rfc821Status.class, mailSystemStatus.getClass());
        assertEquals("5.0.0 error\r\nMissing code!", mailSystemStatus
                .getMessage());
    }
}
