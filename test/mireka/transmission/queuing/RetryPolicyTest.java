package mireka.transmission.queuing;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import mireka.ExampleAddress;
import mireka.ExampleMail;
import mireka.transmission.LocalMailSystemException;
import mireka.transmission.Mail;
import mireka.transmission.Transmitter;
import mireka.transmission.dsn.DsnMailCreator;
import mireka.transmission.immediate.RecipientRejection;
import mireka.transmission.immediate.RecipientsWereRejectedException;
import mireka.transmission.immediate.RemoteMta;
import mireka.transmission.immediate.RemoteMtaErrorResponseException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.subethamail.smtp.client.SMTPException;
import org.subethamail.smtp.client.SMTPClient.Response;

@RunWith(MockitoJUnitRunner.class)
public class RetryPolicyTest {
    @Mock
    private Transmitter mockedRetryTransmitter;
    @Mock
    private DsnMailCreator mockedDsnMailCreator;
    @Mock
    private Transmitter mockedDsnTransmitter;
    private RetryPolicy retryPolicy =
            new RetryPolicy(mockedDsnMailCreator, mockedDsnTransmitter,
                    mockedRetryTransmitter);
    private Mail mail = ExampleMail.simple();
    private RemoteMtaErrorResponseException permanentSendException =
            new RemoteMtaErrorResponseException(new SMTPException(new Response(
                    550, "Example error")), new RemoteMta("mail.example.com"));
    private RemoteMtaErrorResponseException transientSendException =
            new RemoteMtaErrorResponseException(new SMTPException(new Response(
                    400, "Example temporary error")), new RemoteMta(
                    "mail.example.com"));

    @Before
    public void initialize() {
        retryPolicy =
                new RetryPolicy(mockedDsnMailCreator, mockedDsnTransmitter,
                        mockedRetryTransmitter);
    }

    @Test
    public void testOnEntireMailFailurePermanent()
            throws LocalMailSystemException {
        retryPolicy.actOnEntireMailFailure(mail, permanentSendException);
        verify(mockedDsnTransmitter).transmit(any(Mail.class));
    }

    @Test
    public void testOnEntireMailFailureTemporary()
            throws LocalMailSystemException {
        retryPolicy.actOnEntireMailFailure(mail, transientSendException);
        verify(mockedRetryTransmitter).transmit(any(Mail.class));
    }

    @Test
    public void testOnEntireMailFailureGiveUp() throws LocalMailSystemException {
        mail.deliveryAttempts = 100;
        retryPolicy.actOnEntireMailFailure(mail, transientSendException);
        verify(mockedDsnTransmitter).transmit(any(Mail.class));
    }

    @Test
    public void testOnEntireMailFailurePermanentNotification()
            throws LocalMailSystemException {
        mail.from = "";
        retryPolicy.actOnEntireMailFailure(mail, permanentSendException);
        verify(mockedDsnTransmitter, never()).transmit(any(Mail.class));
    }

    @Test
    public void testRecipientsRejected() throws LocalMailSystemException {
        List<RecipientRejection> rejections =
                new ArrayList<RecipientRejection>();
        rejections.add(new RecipientRejection(ExampleAddress.JANE_AS_RECIPIENT,
                permanentSendException));
        rejections.add(new RecipientRejection(ExampleAddress.JOHN_AS_RECIPIENT,
                transientSendException));
        RecipientsWereRejectedException exception =
                new RecipientsWereRejectedException(rejections);

        retryPolicy.actOnRecipientsWereRejected(mail, exception);
        verify(mockedDsnTransmitter).transmit(any(Mail.class));
        verify(mockedRetryTransmitter).transmit(any(Mail.class));
    }

}
