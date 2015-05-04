package mireka.transmission.queue;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import mireka.ExampleMail;
import mireka.transmission.Mail;
import mireka.transmission.queue.dataprop.DataProperties;
import mockit.Tested;

import org.junit.Test;

public class MailEnvelopePersisterTest {

    @Tested
    MailEnvelopePersister persister;

    /**
     * This is just a reminder to update the MailEnvelopePersister if a new
     * field is added to {@link Mail}.
     */
    @Test
    public void testExtraFieldReminder() {
        assertEquals(8, calculateCountOfAttributesToBeStoredInProperties());
    }

    @Test
    public void testSaveAndLoad() {

        Mail s = ExampleMail.simple();
        DataProperties properties = new DataProperties();

        persister.storeMailFieldsIntoProperties(s, properties);

        // 8 - 2 null propertiess
        assertEquals(6, properties.size());

        Mail d = persister.readFromProperties(properties);

        assertEquals(s.from.getSmtpText(), d.from.getSmtpText());
        assertEquals(1, d.recipients.size());
        assertEquals(s.recipients.get(0).sourceRouteStripped(), d.recipients
                .get(0).sourceRouteStripped());
        assertEquals(s.arrivalDate, d.arrivalDate);
        assertEquals(s.receivedFromMtaName, d.receivedFromMtaName);
        assertEquals(s.receivedFromMtaAddress, d.receivedFromMtaAddress);
        assertEquals(s.scheduleDate, d.scheduleDate);
        assertEquals(s.deliveryAttempts, d.deliveryAttempts);
        assertEquals(s.postpones, d.postpones);

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
}
