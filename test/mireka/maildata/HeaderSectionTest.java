package mireka.maildata;

import static mireka.maildata.parser.Kind.*;
import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import mireka.Deencapsulation;
import mireka.maildata.HeaderSection.Entry;
import mireka.maildata.field.AddressListField;
import mireka.maildata.field.UnstructuredField;
import mireka.maildata.type.AddrSpec;
import mireka.maildata.type.Mailbox;

public class HeaderSectionTest {

    private HeaderFieldText from1Text;
    private HeaderFieldText from2Text;
    private HeaderFieldText subjectText;
    private AddressListField from3Header;

    List<HeaderSection.Entry> fields;

    HeaderSection headerSection = new HeaderSection();

    @SuppressWarnings("unchecked")
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

        from3Header = new AddressListField(FROM);
        from3Header.addressList.add(mailboxAddress);

        fields = (List<Entry>) Deencapsulation.getField(headerSection, "fields");
    }

    @Test
    public void testGet() throws ParseException {
        headerSection.addExtracted(from1Text);
        headerSection.addExtracted(from2Text);
        headerSection.addExtracted(subjectText);

        UnstructuredField f = headerSection.get(SUBJECT, UnstructuredField.class);

        assertEquals("Interesting email", f.body);
        assertEquals(" Interesting email", f.bodyFull);
    }

    @Test
    public void testGetAll() throws ParseException {
        headerSection.addExtracted(from1Text);
        headerSection.addExtracted(from2Text);
        headerSection.addExtracted(subjectText);

        List<AddressListField> all = headerSection.getAll(FROM, AddressListField.class);
        assertEquals(2, all.size());
        assertEquals("John Doe",
                ((Mailbox) all.get(0).addressList.get(0)).displayName);
        assertEquals("Jane Doe",
                ((Mailbox) all.get(1).addressList.get(0)).displayName);
    }

    @Test
    public void testAddExtractedPut() {
        headerSection.addExtracted(from1Text);
        headerSection.addExtracted(from2Text);
        headerSection.addExtracted(subjectText);

        headerSection.put(from3Header);

        assertEquals(2, fields.size());
        assertEquals(FROM, fields.get(0).kind);
        assertEquals(SUBJECT, fields.get(1).kind);
    }

    @Test
    public void testRemove() {
        headerSection.addExtracted(from1Text);
        headerSection.addExtracted(from2Text);
        headerSection.addExtracted(subjectText);

        headerSection.remove(FROM);
        assertEquals(1, fields.size());
    }
}
