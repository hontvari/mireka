package mireka.filter.builtin;

import java.io.IOException;

import mireka.ExampleMails;
import mireka.MailDataWithSameContent;
import mireka.filter.Filter;
import mireka.filter.FilterChain;
import mireka.filter.MailData;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

import static org.mockito.Mockito.verify;

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
    public void testSmallMail() throws TooMuchDataException, RejectException,
            IOException {
        filter.data(ExampleMails.simple());

        verify(chain).data(exampleSimpleMail());
    }

    private MailData exampleSimpleMail() {
        return Matchers.argThat(new MailDataWithSameContent(ExampleMails
                .simple()));
    }

    @Test(expected = TooMuchDataException.class)
    public void testLargeMail() throws TooMuchDataException, RejectException,
            IOException {
        filter.data(ExampleMails.mail4k());

        ArgumentCaptor<MailData> producedMailDataArgument =
                ArgumentCaptor.forClass(MailData.class);
        verify(chain).data(producedMailDataArgument.capture());
        MailData producedMailData = producedMailDataArgument.getValue();
        byte[] buffer = new byte[5000]; // larger then allowed
        producedMailData.getInputStream().read(buffer);
    }
}
