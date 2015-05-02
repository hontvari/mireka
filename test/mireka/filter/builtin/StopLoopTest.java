package mireka.filter.builtin;

import java.io.IOException;

import mireka.ExampleMaildata;
import mireka.filter.misc.StopLoop;
import mireka.maildata.Maildata;

import org.junit.Test;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

public class StopLoopTest {

    @Test(expected = RejectException.class)
    public void testLoopingData() throws TooMuchDataException, RejectException,
            IOException {
        StopLoop stopLoop = new StopLoop();
        stopLoop.setMaxReceivedHeaders(2);
        Maildata bouncedMail =
                ExampleMaildata.fromResource(getClass(), "looping.eml");
        stopLoop.data(bouncedMail);
    }

    @Test
    public void testNotLoopingData() throws TooMuchDataException,
            RejectException, IOException {
        StopLoop stopLoop = new StopLoop();
        stopLoop.setMaxReceivedHeaders(2);
        stopLoop.data(ExampleMaildata.simple());
    }
}
