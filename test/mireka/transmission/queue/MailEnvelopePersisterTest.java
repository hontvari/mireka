package mireka.transmission.queue;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import mireka.ExampleMail;
import mireka.transmission.Mail;
import mireka.transmission.queue.dataprop.DataProperties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class MailEnvelopePersisterTest {

    @Test
    public void testAllFieldsAreSaved() {
        CountingMockAnswer countingMockAnswer = new CountingMockAnswer();
        DataProperties mockedProperties =
                Mockito.mock(DataProperties.class, countingMockAnswer);
        MailEnvelopePersister persister = new MailEnvelopePersister();

        persister.storeMailFieldsIntoProperties(ExampleMail.simple(),
                mockedProperties);

        assertEquals(calculateCountOfAttributesToBeStoredInProperties(),
                countingMockAnswer.count);
    }

    @Test
    public void testAllFieldsAreRead() {
        CountingMockAnswer countingMockAnswer = new CountingMockAnswer();
        DataProperties mockedProperties =
                Mockito.mock(DataProperties.class, countingMockAnswer);
        MailEnvelopePersister persister = new MailEnvelopePersister();

        persister.readFromProperties(mockedProperties);

        assertEquals(calculateCountOfAttributesToBeStoredInProperties(),
                countingMockAnswer.count);
    }

    private static int calculateCountOfAttributesToBeStoredInProperties() {
        int c = 0;
        for (Field field : Mail.class.getDeclaredFields()) {
            if (!field.isSynthetic()
                    && !Modifier.isStatic(field.getModifiers()))
                c++;
        }
        int countOfFieldsNotStoredAsProperty = 1; // mailData
        return c - countOfFieldsNotStoredAsProperty;
    }

    public static class CountingMockAnswer implements Answer<Object> {
        public int count = 0;

        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            count++;
            return Mockito.RETURNS_SMART_NULLS.answer(invocation);
        }

    }

}
