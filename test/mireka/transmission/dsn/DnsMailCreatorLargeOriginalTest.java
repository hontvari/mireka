package mireka.transmission.dsn;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import mireka.ExampleMail;
import mireka.transmission.Mail;

import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.junit.Test;

public class DnsMailCreatorLargeOriginalTest {
    @Test
    public void testLongMail() throws Exception {
        DsnMailCreator dsnMailCreator =
                DsnMailCreatorTest.createDsnMailCreator();
        Mail mail = ExampleMail.veryLong();
        List<RecipientProblemReport> recipientStatuses =
                DsnMailCreatorTest.createRecipientFailure();
        Mail dsnMail = dsnMailCreator.create(mail, recipientStatuses);

        File file =
                new File(System.getProperty("java.io.tmpdir"), getClass()
                        .getSimpleName() + ".eml");
        FileOutputStream fout = new FileOutputStream(file);
        dsnMail.maildata.writeTo(fout);
        fout.close();

        FileInputStream in = new FileInputStream(file);
        Message message = new DefaultMessageBuilder().parseMessage(in);
        in.close();
        assertEquals(message.getMimeType(), "multipart/report");
        file.delete();
    }

}
