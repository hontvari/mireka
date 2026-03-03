package mireka.maildata.parser;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import mireka.ByteArrayMaildataSource;
import mireka.maildata.HeaderFieldText;
import mireka.maildata.HeaderSection;
import mireka.maildata.MailMap;
import mockit.Mocked;
import mockit.Verifications;

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
        ByteArrayMaildataSource maildataFile = new ByteArrayMaildataSource(mail);

        MailMap map = new MailMap();
        new MaildataParser(maildataFile.getInputStream()).parse(map);

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

        assertEquals(18, map.bodyRange.start);

    }
}
