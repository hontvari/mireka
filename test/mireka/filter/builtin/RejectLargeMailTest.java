package mireka.filter.builtin;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import mireka.ExampleMaildataFile;
import mireka.IsStreamEquals;
import mireka.filter.Filter;
import mireka.filter.FilterChain;
import mireka.filter.misc.RejectLargeMail;
import mireka.smtp.RejectExceptionExt;
import mockit.Mocked;
import mockit.Verifications;

import org.junit.Before;
import org.junit.Test;
import org.subethamail.smtp.TooMuchDataException;

public class RejectLargeMailTest {
    @Mocked
    private FilterChain chain;
    private Filter filter;

    @Before
    public void setup() {
        RejectLargeMail rejectLargeMail = new RejectLargeMail();
        rejectLargeMail.setMaxAllowedSize(3000);
        filter = rejectLargeMail.createInstance(null);
        filter.setChain(chain);
    }

    @Test
    public void testSmallMail() throws TooMuchDataException,
            RejectExceptionExt, IOException {

        filter.dataStream(ExampleMaildataFile.simple().getInputStream());

        new Verifications() {
            {
                InputStream stream;
                chain.dataStream(stream = withCapture());
                assertThat(stream, new IsStreamEquals(ExampleMaildataFile
                        .simple().getInputStream()));
            }
        };
    }

    @Test(expected = TooMuchDataException.class)
    public void testLargeMail() throws TooMuchDataException,
            RejectExceptionExt, IOException {

        filter.dataStream(ExampleMaildataFile.mail4k().getInputStream());

        new Verifications() {
            {
                InputStream stream;
                chain.dataStream(stream = withCapture());
                stream.read(new byte[5000]); // larger then allowed
            }
        };
    }
}
