package mireka.transmission.queuing;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import mireka.ExampleAddress;
import mireka.ExampleMail;
import mireka.smtp.EnhancedStatus;
import mireka.transmission.LocalMailSystemException;
import mireka.transmission.Mail;
import mireka.transmission.Transmitter;
import mireka.transmission.dsn.DsnMailCreator;
import mireka.transmission.immediate.PostponeException;
import mireka.transmission.immediate.RecipientRejection;
import mireka.transmission.immediate.RecipientsWereRejectedException;
import mireka.transmission.immediate.RemoteMta;
import mireka.transmission.immediate.RemoteMtaErrorResponseException;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;

import org.junit.Before;
import org.junit.Test;
import org.subethamail.smtp.client.SMTPClient.Response;
import org.subethamail.smtp.client.SMTPException;

public class RetryPolicyTest {
    @Mocked
    private Transmitter retryTransmitter;
    @Mocked
    private DsnMailCreator dsnMailCreator;
    @Mocked
    private Transmitter dsnTransmitter;
    private RetryPolicy retryPolicy;
    private Mail mail = ExampleMail.simple();
    private RemoteMtaErrorResponseException permanentSendException =
            new RemoteMtaErrorResponseException(new SMTPException(new Response(
                    550, "Example error")), new RemoteMta("mail.example.com"));
    private RemoteMtaErrorResponseException transientSendException =
            new RemoteMtaErrorResponseException(new SMTPException(new Response(
                    400, "Example temporary error")), new RemoteMta(
                    "mail.example.com"));
    private PostponeException postponeException = new PostponeException(30,
            EnhancedStatus.TRANSIENT_SYSTEM_NOT_ACCEPTING_NETWORK_MESSAGES,
            "Test exception");

    @Before
    public void initialize() {
        retryPolicy =
                new RetryPolicy(dsnMailCreator, dsnTransmitter,
                        retryTransmitter);
    }

    @Test
    public void testOnEntireMailFailurePermanent()
            throws LocalMailSystemException {
        retryPolicy.actOnEntireMailFailure(mail, permanentSendException);

        new Verifications() {
            {
                onInstance(dsnTransmitter).transmit((Mail) any);
            }
        };
    }

    @Test
    public void testOnEntireMailFailureTemporary()
            throws LocalMailSystemException {
        retryPolicy.actOnEntireMailFailure(mail, transientSendException);

        new Verifications() {
            {
                onInstance(retryTransmitter).transmit((Mail) any);
            }
        };
    }

    @Test
    public void testOnEntireMailFailureGiveUp() throws LocalMailSystemException {
        mail.deliveryAttempts = 100;
        retryPolicy.actOnEntireMailFailure(mail, transientSendException);

        new Verifications() {
            {
                onInstance(dsnTransmitter).transmit((Mail) any);
            }
        };
    }

    @Test
    public void testOnEntireMailFailurePermanentNotification()
            throws LocalMailSystemException {
        mail.from = "";
        retryPolicy.actOnEntireMailFailure(mail, permanentSendException);

        new Verifications() {
            {
                onInstance(dsnTransmitter).transmit((Mail) any);
                times = 0;
            }
        };
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

        new Verifications() {
            {
                onInstance(dsnTransmitter).transmit((Mail) any);
                onInstance(retryTransmitter).transmit((Mail) any);
            }
        };
    }

    @Test
    public void testMailPostponedFirst() throws Exception {
        new Expectations() {
            {
                onInstance(retryTransmitter).transmit((Mail) any);
                forEachInvocation = new Object() {
                    @SuppressWarnings("unused")
                    void validate(Mail mail) {
                        assertEquals(0, mail.deliveryAttempts);
                        assertEquals(1, mail.postpones);
                        double actualDelay =
                                (mail.scheduleDate.getTime() - System
                                        .currentTimeMillis()) / 1000;
                        assertEquals(postponeException.getRecommendedDelay(),
                                actualDelay, 10);
                    }
                };
            }
        };
        retryPolicy.actOnPostponeRequired(mail, postponeException);
    }

    @Test
    public void testMailPostponedRepeatedly() throws Exception {
        new Expectations() {
            {
                onInstance(retryTransmitter).transmit((Mail) any);
                forEachInvocation = new Object() {
                    @SuppressWarnings("unused")
                    void validate(Mail mail) {
                        assertEquals(1, mail.deliveryAttempts);
                        assertEquals(0, mail.postpones);
                    }
                };
            }
        };
        mail.postpones = 3;
        retryPolicy.actOnPostponeRequired(mail, postponeException);
    }

}
