package mireka.maildata;

import static org.junit.Assert.*;
import mireka.maildata.field.FromField;
import mireka.maildata.field.To;
import mireka.maildata.field.UnstructuredField;

import org.junit.Before;
import org.junit.Test;

public class FieldGeneratorTest {

    private Mailbox john;
    private Mailbox jane;
    private Group gourmets;

    @Before
    public void setUp() {
        john = new Mailbox();
        john.displayName = "John Doe";
        john.addrSpec = new AddrSpec();
        john.addrSpec.localPart = "john";
        john.addrSpec.domain = new DotAtomDomainPart("example.com");

        jane = new Mailbox();
        jane.displayName = null;
        jane.addrSpec = new AddrSpec();
        jane.addrSpec.localPart = "jane";
        jane.addrSpec.domain = new DotAtomDomainPart("example.com");

        gourmets = new Group();
        gourmets.displayName = "Gourmets";
        gourmets.mailboxList.add(john);
        gourmets.mailboxList.add(jane);
    }

    @Test
    public void testUnstructured() {
        UnstructuredField header = new UnstructuredField();
        header.setName("Subject");
        header.body = " To Do Today";
        String result = new FieldGenerator().writeUnstructuredHeader(header);
        assertEquals("Subject: To Do Today\r\n", result);
    }

    @Test
    public void testUnstructuredFolded() {
        UnstructuredField header = new UnstructuredField();
        header.setName("Subject");
        header.body =
                " abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz";
        String result = new FieldGenerator().writeUnstructuredHeader(header);
        assertEquals(
                "Subject: abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz\r\n"
                        + " abcdefghijklmnopqrstuvwxyz\r\n", result);
    }

    @Test
    public void testUnstructuredEncodedWord() {
        UnstructuredField header = new UnstructuredField();
        header.setName("Subject");
        header.body = " tyúk";
        String result = new FieldGenerator().writeUnstructuredHeader(header);
        assertEquals("Subject:=?UTF-8?Q?_ty=C3=BAk?=\r\n", result);
    }

    @Test
    public void testUnstructuredFakeEncodedWord() {
        UnstructuredField header = new UnstructuredField();
        header.setName("Subject");
        header.body = "=?X?=";
        String result = new FieldGenerator().writeUnstructuredHeader(header);
        assertEquals("Subject:=?UTF-8?Q?=3D=3FX=3F=3D?=\r\n", result);
    }

    @Test
    public void testFrom() {
        FromField h = new FromField();
        john.displayName = "Jon Postel";
        john.addrSpec.localPart = "jon";
        john.addrSpec.domain = new DotAtomDomainPart("example.net");
        h.mailboxList.add(john);

        String result = new FieldGenerator().writeFromField(h);

        assertEquals("From: Jon Postel <jon@example.net>\r\n", result);
    }

    @Test
    public void testFromWithoutDisplayName() {
        FromField h = new FromField();
        john.displayName = null;
        h.mailboxList.add(john);

        String result = new FieldGenerator().writeFromField(h);

        assertEquals("From: john@example.com\r\n", result);
    }

    @Test
    public void testFromQuotedDisplayName() {
        FromField h = new FromField();
        john.displayName = "Jane H. Doe";
        john.addrSpec.localPart = "jane";
        h.mailboxList.add(john);

        String result = new FieldGenerator().writeFromField(h);

        assertEquals("From: \"Jane H. Doe\" <jane@example.com>\r\n", result);
    }

    @Test
    public void testFromEncodedWordDisplayName() {
        FromField h = new FromField();
        john.displayName = "Hontvári Levente";
        john.addrSpec.localPart = "levi";
        h.mailboxList.add(john);

        String result = new FieldGenerator().writeFromField(h);

        assertEquals(
                "From: =?UTF-8?Q?Hontv=C3=A1ri_Levente?= <levi@example.com>\r\n",
                result);
    }

    @Test
    public void testFromFakeEncodedWordInDisplayName() {
        FromField h = new FromField();
        john.displayName = "=?John?= TheKing";
        h.mailboxList.add(john);

        String result = new FieldGenerator().writeFromField(h);

        assertEquals("From: \"=?John?= TheKing\" <john@example.com>\r\n",
                result);
    }

    @Test
    public void testFromFakeEncodedWordInDisplayNameLater() {
        FromField h = new FromField();
        john.displayName = "John =?TheKing?=";
        h.mailboxList.add(john);

        String result = new FieldGenerator().writeFromField(h);

        assertEquals("From: \"John =?TheKing?=\" <john@example.com>\r\n",
                result);
    }

    @Test
    public void testTo() {
        To f = new To();
        f.addressList.add(jane);

        String result = new FieldGenerator().writeAddressListField(f);

        assertEquals("To: jane@example.com\r\n", result);

    }

    @Test
    public void testToWithGroup() {
        To f = new To();
        f.addressList.add(jane);
        f.addressList.add(gourmets);
        f.addressList.add(john);

        String result = new FieldGenerator().writeAddressListField(f);

        assertEquals(
                "To: jane@example.com,\r\n"
                        + " Gourmets: John Doe <john@example.com>, jane@example.com;,\r\n"
                        + " John Doe <john@example.com>\r\n", result);
    }

    @Test
    public void testToWithEmptyGroup() {
        To f = new To();
        gourmets.mailboxList.clear();
        f.addressList.add(gourmets);

        String result = new FieldGenerator().writeAddressListField(f);

        assertEquals("To: Gourmets: ;\r\n", result);
    }

}
