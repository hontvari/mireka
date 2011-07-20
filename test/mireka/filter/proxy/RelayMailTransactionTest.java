package mireka.filter.proxy;

import mireka.ExampleAddress;
import mireka.ExampleMail;
import mireka.address.NullReversePath;
import mireka.destination.Session;
import mireka.filter.RecipientContext;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;

import org.junit.Before;
import org.junit.Test;
import org.subethamail.smtp.client.SmartClient;

public class RelayMailTransactionTest {
    @Mocked
    private BackendServer backendServer;

    @NonStrict
    @Mocked(stubOutClassInitialization = false)
    private SmartClient smartClient;

    private RecipientContext recipientContextJane = new RecipientContext(null,
            ExampleAddress.JANE_AS_RECIPIENT);
    private RecipientContext recipientContextJohn = new RecipientContext(null,
            ExampleAddress.JOHN_AS_RECIPIENT);

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
                backendServer.connect();
                result = smartClient;

                smartClient.dataEnd();
                times = 1;

                smartClient.quit();
            }
        };

        session.from(new NullReversePath());
        session.recipient(recipientContextJane);
        session.data(ExampleMail.simple());
    }

    @Test
    public final void testTwoRecipientsSingleDestination() throws Exception {

        new Expectations() {
            {
                backendServer.connect();
                result = smartClient;

                smartClient.to(anyString);
                times = 2;
                smartClient.dataEnd();
                times = 1;
            }
        };

        session.from(new NullReversePath());
        session.recipient(recipientContextJane);
        session.recipient(recipientContextJohn);

        session.data(ExampleMail.simple());
    }
}
