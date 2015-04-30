package mireka.maildata.parser;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import mireka.maildata.HeaderFieldText;
import mireka.maildata.HeaderSection;
import mireka.maildata.parser.MaildataParser;
import mockit.Mocked;
import mockit.Verifications;

import org.junit.Test;

public class MailDataParserTest {
    @Test
    public void testParse(@Mocked final HeaderSection headerSection)
            throws ParseException, IOException {

        // @formatter:off
        String mail = ""
                + "H: a\r\n"
                + "L: a\r\n"
                + " b\r\n"
                + "\r\n"
                + "Hello\r\n";
        // @formatter:on

        InputStream mailInputStream = new ByteArrayInputStream(
                mail.getBytes("US-ASCII"));

        MaildataParser.MaildataMap maildataMap = new MaildataParser(mailInputStream)
                .parse();

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
