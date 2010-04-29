package mireka.submission;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import mireka.transmission.EnhancedStatus;
import mireka.transmission.MailSystemStatus;
import mireka.transmission.immediate.Rfc821Status;
import mireka.transmission.immediate.ResponseParser;

import org.junit.Test;
import org.subethamail.smtp.client.SMTPClient.Response;

public class ResponseFactoryTest {

    private static final String ENHANCED_EXAMPLE =
            "5.7.1 Delivery not authorized, message refused";
    private static final String ORIGINAL_EXAMPLE =
            "Delivery not authorized, message refused";

    @Test
    public void testCreateResponseLookingForEnhancedStatusCode_original() {
        Response srcResponse = new Response(500, ENHANCED_EXAMPLE);
        MailSystemStatus basicResponse =
                new ResponseParser()
                        .createResponseLookingForEnhancedStatusCode(srcResponse);
        assertThat(basicResponse, instanceOf(EnhancedStatus.class));
        EnhancedStatus enhancedResponse = (EnhancedStatus) basicResponse;
        assertEquals("5.7.1", enhancedResponse.getEnhancedStatusCode());

        assertEquals("Delivery not authorized, message refused",
                enhancedResponse.getMessage());
        assertEquals(500, enhancedResponse.getSmtpReplyCode());
    }

    @Test
    public void testCreateResponseLookingForEnhancedStatusCode_enhanced() {
        Response srcResponse = new Response(500, ORIGINAL_EXAMPLE);
        MailSystemStatus basicResponse =
                new ResponseParser()
                        .createResponseLookingForEnhancedStatusCode(srcResponse);
        assertThat(basicResponse, instanceOf(Rfc821Status.class));

        assertEquals("Delivery not authorized, message refused", basicResponse
                .getMessage());
        assertEquals(500, basicResponse.getSmtpReplyCode());
    }
}
