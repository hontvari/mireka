package mireka;

import static mireka.ExampleAddress.*;

import java.util.Date;

import mireka.maildata.Maildata;
import mireka.transmission.Mail;

public class ExampleMail {
    /**
     * Returns a short mail sent to Jane from John.
     */
    public static Mail simple() {
        Mail result = new Mail();
        result.arrivalDate = new Date();
        result.from = JOHN_AS_REVERSE_PATH;
        result.receivedFromMtaAddress = IP1;
        result.receivedFromMtaName = IP1.getHostName();
        result.recipients.add(JANE_AS_RECIPIENT);
        result.scheduleDate = result.arrivalDate;
        result.maildata = ExampleMaildata.simple();
        return result;
    }

    /**
     * returns a mail suitable to test Out of Memory conditions
     */
    public static Mail veryLong() {
        Mail result = simple();
        result.maildata = new Maildata(new LongMaildataFile());
        return result;
    }

}
