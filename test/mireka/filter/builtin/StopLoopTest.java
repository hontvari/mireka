package mireka.filter.builtin;

import mireka.ExampleMaildata;
import mireka.filter.MailTransaction;
import mireka.filter.misc.StopLoop;

import org.junit.Test;
import org.subethamail.smtp.RejectException;

public class StopLoopTest {

    private MailTransaction transaction = new MailTransaction(null);

    @Test(expected = RejectException.class)
    public void testLoopingData() throws RejectException {
        StopLoop stopLoop = new StopLoop();
        stopLoop.setMaxReceivedHeaders(2);
        transaction.data =
                ExampleMaildata.fromResource(getClass(), "looping.eml");
        stopLoop.data(transaction);
    }

    @Test
    public void testNotLoopingData() throws RejectException {
        StopLoop stopLoop = new StopLoop();
        stopLoop.setMaxReceivedHeaders(2);
        transaction.data = ExampleMaildata.simple();
        stopLoop.data(transaction);
    }
}
