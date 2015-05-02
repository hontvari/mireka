package mireka.filter.builtin;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;

import mireka.ExampleMaildataFile;
import mireka.IsStreamEquals;
import mireka.filter.Filter;
import mireka.filter.FilterChain;
import mireka.filter.misc.RejectLargeMail;
import mireka.smtp.RejectExceptionExt;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.subethamail.smtp.TooMuchDataException;

public class RejectLargeMailTest {
    @Mock
    private FilterChain chain;
    private Filter filter;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        RejectLargeMail rejectLargeMail = new RejectLargeMail();
        rejectLargeMail.setMaxAllowedSize(3000);
        filter = rejectLargeMail.createInstance(null);
        filter.setChain(chain);
    }

    @Test
    public void testSmallMail() throws TooMuchDataException,
            RejectExceptionExt, IOException {
        filter.dataStream(ExampleMaildataFile.simple().getInputStream());

        verify(chain).dataStream(exampleSimpleMail());
    }

    private InputStream exampleSimpleMail() {
        return Matchers.argThat(new IsStreamEquals(ExampleMaildataFile.simple()
                .getInputStream()));
    }

    @Test(expected = TooMuchDataException.class)
    public void testLargeMail() throws TooMuchDataException,
            RejectExceptionExt, IOException {
        filter.dataStream(ExampleMaildataFile.mail4k().getInputStream());

        ArgumentCaptor<InputStream> producedInputStreamArgument =
                ArgumentCaptor.forClass(InputStream.class);
        verify(chain).dataStream(producedInputStreamArgument.capture());
        InputStream producedInputStream =
                producedInputStreamArgument.getValue();
        byte[] buffer = new byte[5000]; // larger then allowed
        producedInputStream.read(buffer);
    }
}
