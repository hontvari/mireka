package mireka.maildata;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.List;

import mireka.maildata.field.From;
import mockit.Deencapsulation;

import org.junit.Before;
import org.junit.Test;

public class HeaderSectionTest {

    private HeaderFieldText from1Text;
    private HeaderFieldText from2Text;
    private HeaderFieldText subjectText;
    private From from3Header;

    @Before
    public void setUp() throws ParseException {
        from1Text = new HeaderFieldText();
        from1Text.originalSpelling =
                from1Text.unfoldedSpelling =
                        "From: John Doe <john@example.com>";
        from2Text = new HeaderFieldText();
        from2Text.originalSpelling =
                from2Text.unfoldedSpelling =
                        "From: Jane Doe <jane@example.com>";
        subjectText = new HeaderFieldText();
        subjectText.originalSpelling =
                subjectText.unfoldedSpelling = "Subject: Interesting email";

        Mailbox mailboxAddress = new Mailbox();
        mailboxAddress.displayName = "John Doe via alist.example.com";
        mailboxAddress.addrSpec =
                AddrSpec.fromString("placeholder@example.com");

        from3Header = new From();
        from3Header.setName("From");
        from3Header.mailboxList.add(mailboxAddress);

    }

    @Test
    public void testAddExtractedAndUpdate() throws ParseException {
        HeaderSection headerSection = new HeaderSection();
        headerSection.addExtracted(from1Text);
        headerSection.addExtracted(from2Text);
        headerSection.addExtracted(subjectText);

        headerSection.updateOrAppend(from3Header);

        List<HeaderSection.Entry> fields =
                Deencapsulation.getField(headerSection, "fields");
        assertEquals(2, fields.size());
        assertEquals("from", fields.get(0).lowerCaseName);
        assertEquals("subject", fields.get(1).lowerCaseName);
    }

}
