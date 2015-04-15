package mireka.maildata;

import static org.junit.Assert.*;

import java.text.ParseException;

import org.junit.Test;

public class FieldParserTest {
    @Test
    public void testParseAddress() throws ParseException {
        AddrSpec addrSpec = new FieldParser().parseAddrSpec("john@example.com");

        assertEquals("john", addrSpec.localPart);
        assertTrue(addrSpec.domain instanceof DotAtomDomainPart);
        assertEquals("example.com",
                ((DotAtomDomainPart) addrSpec.domain).domain);

    }

    @Test
    public void testParseStrangeAddress() throws ParseException {
        // RFC 822 example
        AddrSpec addrSpec =
                new FieldParser().parseAddrSpec("God@heaven. af.mil");

        assertEquals("God", addrSpec.localPart);
        assertTrue(addrSpec.domain instanceof DotAtomDomainPart);
        assertEquals("heaven.af.mil",
                ((DotAtomDomainPart) addrSpec.domain).domain);

    }

    @Test
    public void testParseAddressWithDomainLiteral() throws ParseException {
        AddrSpec addrSpec = new FieldParser().parseAddrSpec("john@[127.0.0.1]");

        assertEquals("john", addrSpec.localPart);
        assertTrue(addrSpec.domain instanceof LiteralDomainPart);
        assertEquals("127.0.0.1", ((LiteralDomainPart) addrSpec.domain).literal);

    }

    @Test
    public void testUnstructuredField() throws ParseException {
        UnstructuredHeader header =
                (UnstructuredHeader) new FieldParser()
                        .parseField("Subject: Hello world!\r\n");

        assertEquals("Subject", header.name);
        assertEquals("subject", header.lowerCaseName);
        assertEquals(" Hello world!", header.body);
    }

    @Test
    public void testFromField() throws ParseException {
        FromHeader header =
                (FromHeader) new FieldParser()
                        .parseField("From: john@example.com\r\n");

        assertEquals("From", header.name);
        assertEquals("from", header.lowerCaseName);
        assertEquals(1, header.mailboxList.size());
        Mailbox mailbox1 = header.mailboxList.get(0);
        assertEquals("john", mailbox1.addrSpec.localPart);
        assertNull(mailbox1.displayName);
    }

    @Test
    public void testFromFieldList() throws ParseException {
        FromHeader header =
                (FromHeader) new FieldParser()
                        .parseField("From: john@example.com, Jane Doe <jane@example.com>, "
                                + ", \"Jannie Doe\" <jannie@example.com>\r\n");

        assertEquals(3, header.mailboxList.size());
        Mailbox mailbox1 = header.mailboxList.get(0);
        Mailbox mailbox2 = header.mailboxList.get(1);
        Mailbox mailbox3 = header.mailboxList.get(2);
        assertEquals("john", mailbox1.addrSpec.localPart);
        assertNull(mailbox1.displayName);
        assertEquals("jane", mailbox2.addrSpec.localPart);
        assertEquals("Jane Doe", mailbox2.displayName);
        assertEquals("jannie", mailbox3.addrSpec.localPart);
        assertEquals("Jannie Doe", mailbox3.displayName);
    }

    @Test
    public void testFromFieldWithEncodedName() throws ParseException {
        FromHeader header =
                (FromHeader) new FieldParser()
                        .parseField("From: =?US-ASCII?Q?Keith_Moore?= <moore@example.org>\r\n");

        assertEquals(1, header.mailboxList.size());
        Mailbox mailbox1 = header.mailboxList.get(0);
        assertEquals("moore", mailbox1.addrSpec.localPart);
        assertEquals("Keith Moore", mailbox1.displayName);
    }

    @Test
    public void testFromFieldWithMultiEncodedName() throws ParseException {
        FromHeader header =
                (FromHeader) new FieldParser()
                        .parseField("From: =?US-ASCII?Q?Keith_Mo?= =?US-ASCII?Q?ore?= <moore@example.org>\r\n");

        assertEquals(1, header.mailboxList.size());
        Mailbox mailbox1 = header.mailboxList.get(0);
        assertEquals("moore", mailbox1.addrSpec.localPart);
        assertEquals("Keith Moore", mailbox1.displayName);
    }

}
