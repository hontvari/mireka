package mireka.filter.proxy;

import mireka.ExampleAddress;
import mireka.ExampleMail;
import mireka.address.NullReversePath;
import mireka.destination.Session;
import mireka.filter.RecipientContext;
import mireka.smtp.client.BackendServer;
import mireka.smtp.client.SmtpClient;
import mockit.Expectations;
import mockit.Mocked;

import org.junit.Before;
import org.junit.Test;

public class RelayMailTransactionTest {
    @Mocked
    private BackendServer backendServer;

    @Mocked(stubOutClassInitialization = false)
    private SmtpClient client;

    private final RecipientContext recipientContextJane = new RecipientContext(
            null, ExampleAddress.JANE_AS_RECIPIENT);
    private final RecipientContext recipientContextJohn = new RecipientContext(
            null, ExampleAddress.JOHN_AS_RECIPIENT);

    private Session session;

    @Before
    public void setup() {
        RelayDestination destination = new RelayDestination();
        destination.setBackendServer(backendServer);
        session = destination.createSession();
    }

    @Test
    public final void testSingleRecipient() throws Exception {

        new Expectations() {
            {
                backendServer.createClient();
                result = client;

                client.dataEnd();
                times = 1;

                client.quit();
            }
        };

        session.from(new NullReversePath());
        session.recipient(recipientContextJane);
        session.data(ExampleMail.simple());
        session.done();
    }

    @Test
    public final void testTwoRecipientsSingleDestination() throws Exception {

        new Expectations() {
            {
                backendServer.createClient();
                result = client;

                client.to(anyString);
                times = 2;
                client.dataEnd();
                times = 1;
            }
        };

        session.from(new NullReversePath());
        session.recipient(recipientContextJane);
        session.recipient(recipientContextJohn);

        session.data(ExampleMail.simple());
    }
}
