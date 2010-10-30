package mireka.filter.proxy;

import mireka.ExampleAddress;
import mireka.ExampleMailData;
import mireka.filter.RecipientContext;
import mireka.filter.local.table.Relay;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;

import org.junit.Before;
import org.junit.Test;
import org.subethamail.smtp.client.SmartClient;

public class RelayMailTransactionTest {
    @Mocked
    private BackendServer backendServerA;
    @Mocked
    private BackendServer backendServerB;

    @NonStrict
    @Mocked(stubOutClassInitialization = false)
    private SmartClient smartClientA;
    @NonStrict
    @Mocked(stubOutClassInitialization = false)
    private SmartClient smartClientB;

    private RecipientContext recipientContextJane = new RecipientContext(
            ExampleAddress.JANE_AS_RECIPIENT);
    private RecipientContext recipientContextJohn = new RecipientContext(
            ExampleAddress.JOHN_AS_RECIPIENT);

    private RelayMailTransaction.FilterImpl filter =
            new RelayMailTransaction.FilterImpl(null);

    private Relay destinationA;
    private Relay destinationB;

    @Before
    public void setup() {
        destinationA = new Relay();
        destinationA.setBackendServer(backendServerA);
        destinationB = new Relay();
        destinationB.setBackendServer(backendServerB);
    }

    @Test
    public final void testSingleRecipient() throws Exception {

        new Expectations() {
            {
                backendServerA.connect();
                result = smartClientA;

                smartClientA.dataEnd();
                times = 1;
            }
        };

        recipientContextJane.setDestination(destinationA);

        filter.recipient(recipientContextJane);

        filter.data(ExampleMailData.simple());
    }

    @Test
    public final void testTwoRecipientsSingleDestination() throws Exception {

        new Expectations() {
            {
                backendServerA.connect();
                result = smartClientA;

                smartClientA.to(anyString);
                times = 2;
                smartClientA.dataEnd();
                times = 1;
            }
        };

        recipientContextJane.setDestination(destinationA);
        recipientContextJohn.setDestination(destinationA);

        filter.recipient(recipientContextJane);
        filter.recipient(recipientContextJohn);

        filter.data(ExampleMailData.simple());
    }

    @Test
    public final void testTwoRecipientsTwoDestinations() throws Exception {

        new Expectations() {
            {
                backendServerA.connect();
                result = smartClientA;
                backendServerB.connect();
                result = smartClientB;

                smartClientA.to(anyString);
                times = 1;
                smartClientA.dataEnd();
                times = 1;

                smartClientB.to(anyString);
                times = 1;
                smartClientB.dataEnd();
                times = 1;

            }
        };

        recipientContextJane.setDestination(destinationA);
        recipientContextJohn.setDestination(destinationB);

        filter.recipient(recipientContextJane);
        filter.recipient(recipientContextJohn);

        filter.data(ExampleMailData.simple());
    }
}
