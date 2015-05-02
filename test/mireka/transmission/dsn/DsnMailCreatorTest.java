package mireka.transmission.dsn;

import static mireka.ExampleAddress.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import mireka.ExampleMail;
import mireka.address.MailAddressFactory;
import mireka.maildata.Maildata;
import mireka.smtp.EnhancedStatus;
import mireka.smtp.client.MtaAddress;
import mireka.transmission.Mail;
import mireka.transmission.immediate.Rfc821Status;

import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.junit.Test;
import org.subethamail.smtp.client.SMTPClient.Response;

public class DsnMailCreatorTest {

    @Test
    public void testCreate() throws Exception {
        DsnMailCreator dsnMailCreator = createDsnMailCreator();
        Mail mail = ExampleMail.simple();
        List<RecipientProblemReport> recipientStatuses =
                createRecipientFailure();
        Mail dsnMail = dsnMailCreator.create(mail, recipientStatuses);

        writeToFileForDebugging(dsnMail.maildata);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        dsnMail.maildata.writeTo(out);
        byte[] bytes = out.toByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        Message message = new DefaultMessageBuilder().parseMessage(in);
        assertEquals(message.getMimeType(), "multipart/report");
    }

    static DsnMailCreator createDsnMailCreator() {
        NameAddr from =
                new NameAddr("Mail Delivery Subsystem",
                        "mailer-daemon@example.com");
        DsnMailCreator dsnMailCreator = new DsnMailCreator();
        dsnMailCreator.setReportingMtaName(IP2.getHostName());
        dsnMailCreator.setFromAddress(from);
        return dsnMailCreator;
    }

    static List<RecipientProblemReport> createRecipientFailure()
            throws ParseException {
        PermanentFailureReport f = new PermanentFailureReport();
        f.recipient =
                new MailAddressFactory().createRecipient("jane@example.com");
        // f.status =
        // new EnhancedStatus(550, "5.2.1",
        // "Mailbox disabled, not accepting messages");

        f.status = new EnhancedStatus(550, "5.2.1", longErrorMessage());
        f.remoteMtaDiagnosticStatus =
                new Rfc821Status(new Response(550,
                        "Requested action not taken: mailbox unavailable"));
        f.remoteMta = new MtaAddress(HOST3_EXAMPLE_COM, IP3);
        f.failureDate = new Date();
        f.logId = "NO_1_ENTRY";
        return Collections.singletonList((RecipientProblemReport) f);
    }

    private static String longErrorMessage() {
        String s = "";
        for (int i = 0; i < 100; i++) {
            s += "0123456789 ";
        }
        return s;
    }

    private void writeToFileForDebugging(Maildata content)
            throws FileNotFoundException, IOException {
        File file =
                new File(System.getProperty("java.io.tmpdir"), getClass()
                        .getSimpleName() + ".eml");
        FileOutputStream fout = new FileOutputStream(file);
        content.writeTo(fout);
        fout.close();
    }

}
