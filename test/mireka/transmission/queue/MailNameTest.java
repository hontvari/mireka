package mireka.transmission.queue;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import mireka.transmission.queue.MailName;

import org.junit.Test;

public class MailNameTest {
    private final Date testDate;

    public MailNameTest() {
        Calendar testCalendar =
                Calendar.getInstance(TimeZone.getTimeZone("GMT+2"), Locale.US);
        testCalendar.set(2009, 11, 29, 12, 0, 0);
        testCalendar.set(Calendar.MILLISECOND, 500);
        testDate = testCalendar.getTime();
    }

    @Test
    public void testMailName_sequence_0() {
        MailName name1 = MailName.create(testDate);
        MailName name2 = new MailName(name1.envelopeFileName());
        assertEquals(name1.scheduleDate, name2.scheduleDate);
    }

    @Test
    public void testMailName_sequence_non_0() {
        MailName name1 = MailName.create(testDate).nextInSequence();
        MailName name2 = new MailName(name1.baseFileName + ".xyz");
        assertEquals(name1.scheduleDate, name2.scheduleDate);
        assertEquals(name1.sequenceNumber, name2.sequenceNumber);
    }

    @Test
    public void testCompare() {
        MailName name1 = MailName.create(testDate);
        MailName name2 = MailName.create(new Date(testDate.getTime() + 1));
        assertTrue(name1.compareTo(name2) < 0);
    }
}
