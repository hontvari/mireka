package mireka.maildata;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class FieldGeneratorTest {

    private Mailbox mailbox;

    @Before
    public void setUp() {
        mailbox = new Mailbox();
        mailbox.displayName = "John Doe";
        AddrSpec addSpec = new AddrSpec();
        addSpec.localPart = "john";
        DomainPart domain = new DotAtomDomainPart("example.com");
        addSpec.domain = domain;
        mailbox.addrSpec = addSpec;
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
        mailbox.displayName = "Jon Postel";
        mailbox.addrSpec.localPart = "jon";
        mailbox.addrSpec.domain = new DotAtomDomainPart("example.net");
        h.mailboxList.add(mailbox);

        String result = new FieldGenerator().writeFromHeader(h);

        assertEquals("From: Jon Postel <jon@example.net>\r\n", result);
    }

    @Test
    public void testFromWithoutDisplayName() {
        FromField h = new FromField();
        mailbox.displayName = null;
        h.mailboxList.add(mailbox);

        String result = new FieldGenerator().writeFromHeader(h);

        assertEquals("From: john@example.com\r\n", result);
    }

    @Test
    public void testFromQuotedDisplayName() {
        FromField h = new FromField();
        mailbox.displayName = "Jane H. Doe";
        mailbox.addrSpec.localPart = "jane";
        h.mailboxList.add(mailbox);

        String result = new FieldGenerator().writeFromHeader(h);

        assertEquals("From: \"Jane H. Doe\" <jane@example.com>\r\n", result);
    }

    @Test
    public void testFromEncodedWordDisplayName() {
        FromField h = new FromField();
        mailbox.displayName = "Hontvári Levente";
        mailbox.addrSpec.localPart = "levi";
        h.mailboxList.add(mailbox);

        String result = new FieldGenerator().writeFromHeader(h);

        assertEquals(
                "From: =?UTF-8?Q?Hontv=C3=A1ri_Levente?= <levi@example.com>\r\n",
                result);
    }

    @Test
    public void testFromFakeEncodedWordInDisplayName() {
        FromField h = new FromField();
        mailbox.displayName = "=?John?= TheKing";
        h.mailboxList.add(mailbox);

        String result = new FieldGenerator().writeFromHeader(h);

        assertEquals("From: \"=?John?= TheKing\" <john@example.com>\r\n",
                result);
    }

    @Test
    public void testFromFakeEncodedWordInDisplayNameLater() {
        FromField h = new FromField();
        mailbox.displayName = "John =?TheKing?=";
        h.mailboxList.add(mailbox);

        String result = new FieldGenerator().writeFromHeader(h);

        assertEquals("From: \"John =?TheKing?=\" <john@example.com>\r\n",
                result);
    }
}
