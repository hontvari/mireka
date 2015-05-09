package mireka.filter.builtin;

import static org.junit.Assert.*;

import java.io.IOException;

import mireka.ExampleMaildataFile;
import mireka.IsStreamEquals;
import mireka.filter.FilterSession;
import mireka.filter.MailTransaction;
import mireka.filter.misc.RejectLargeMail;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Verifications;

import org.junit.Before;
import org.junit.Test;
import org.subethamail.smtp.TooMuchDataException;

public class RejectLargeMailTest {
    @Mocked
    // Do not mock the entire type because the tested object is the same type
    @Injectable
    private FilterSession nextLink;

    private FilterSession tested;

    private MailTransaction transaction = new MailTransaction(null);

    @Before
    public void setup() {
        RejectLargeMail rejectLargeMail = new RejectLargeMail();
        rejectLargeMail.setMaxAllowedSize(3000);
        tested = rejectLargeMail.createSession();
        tested.setNextLink(nextLink);
        tested.setMailTransaction(transaction);
    }

    @Test
    public void testSmallMail() {

        transaction.dataStream =
                ExampleMaildataFile.simple().getInputStream();
        tested.dataStream();

        new Verifications() {
            {
                nextLink.dataStream();
            }
        };

        assertThat(transaction.dataStream, new IsStreamEquals(
                ExampleMaildataFile.simple().getInputStream()));
    }

    @Test(expected = TooMuchDataException.class)
    public void testLargeMail() throws TooMuchDataException, IOException {

        transaction.dataStream =
                ExampleMaildataFile.mail4k().getInputStream();

        tested.dataStream();

        new Verifications() {
            {
                nextLink.dataStream();
            }
        };

        transaction.dataStream.read(new byte[5000]); // larger then allowed
    }
}
