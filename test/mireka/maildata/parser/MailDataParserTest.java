package mireka.maildata.parser;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import mireka.ByteArrayMaildataFile;
import mireka.maildata.HeaderFieldText;
import mireka.maildata.HeaderSection;
import mockit.Mocked;
import mockit.Verifications;

import org.junit.Test;

public class MailDataParserTest {
    @Test
    public void testParse(@Mocked final HeaderSection headerSection) {

        // @formatter:off
        String mail = ""
                + "H: a\r\n"
                + "L: a\r\n"
                + " b\r\n"
                + "\r\n"
                + "Hello\r\n";
        // @formatter:on

        @SuppressWarnings("resource")
        ByteArrayMaildataFile maildataFile = new ByteArrayMaildataFile(mail);

        MaildataParser.MaildataMap maildataMap =
                new MaildataParser(maildataFile.getInputStream()).parse();

        new Verifications() {
            {
                List<HeaderFieldText> texts = new ArrayList<HeaderFieldText>();

                headerSection.addExtracted(withCapture(texts));

                assertEquals(2, texts.size());
                assertEquals("H: a\r\n", texts.get(0).originalSpelling);
                assertEquals("H: a", texts.get(0).unfoldedSpelling);
                assertEquals("L: a\r\n b\r\n", texts.get(1).originalSpelling);
                assertEquals("L: a b", texts.get(1).unfoldedSpelling);

            }
        };

        assertEquals(18, maildataMap.bodyPosition);

    }
}
