package mireka.maildata;

import static org.junit.Assert.*;

import java.text.ParseException;

import mireka.maildata.field.AddressListField;
import mireka.maildata.field.From;
import mireka.maildata.field.To;

import org.junit.Test;

public class StructuredFieldBodyParserTest {
    @Test
    public void testParseAddress() throws ParseException {
        AddrSpec addrSpec =
                new StructuredFieldBodyParser("john@example.com")
                        .parseAddrSpec();

        assertEquals("john", addrSpec.localPart);
        assertTrue(addrSpec.domain instanceof DotAtomDomainPart);
        assertEquals("example.com",
                ((DotAtomDomainPart) addrSpec.domain).domain);

    }

    @Test
    public void testParseStrangeAddress() throws ParseException {
        // RFC 822 example
        AddrSpec addrSpec =
                new StructuredFieldBodyParser("God@heaven. af.mil")
                        .parseAddrSpec();

        assertEquals("God", addrSpec.localPart);
        assertTrue(addrSpec.domain instanceof DotAtomDomainPart);
        assertEquals("heaven.af.mil",
                ((DotAtomDomainPart) addrSpec.domain).domain);

    }

    @Test
    public void testParseAddressWithDomainLiteral() throws ParseException {
        AddrSpec addrSpec =
                new StructuredFieldBodyParser("john@[127.0.0.1]")
                        .parseAddrSpec();

        assertEquals("john", addrSpec.localPart);
        assertTrue(addrSpec.domain instanceof LiteralDomainPart);
        assertEquals("127.0.0.1", ((LiteralDomainPart) addrSpec.domain).literal);

    }

    @Test
    public void testFromField() throws ParseException {
        From field = new From();
        new StructuredFieldBodyParser(" john@example.com")
                .parseAddressListFieldInto(field);

        assertEquals(1, field.addressList.size());
        Address address = field.addressList.get(0);
        assertEquals(Mailbox.class, address.getClass());
        Mailbox mailbox = (Mailbox) address;
        assertEquals("john", mailbox.addrSpec.localPart);
        assertNull(mailbox.displayName);
    }

    @Test
    public void testFromFieldList() throws ParseException {
        From field = new From();
        new StructuredFieldBodyParser(
                " john@example.com, Jane Doe <jane@example.com>, "
                        + ", \"Jannie Doe\" <jannie@example.com>")
                .parseAddressListFieldInto(field);

        assertEquals(3, field.addressList.size());
        assertEquals(Mailbox.class, field.addressList.get(0).getClass());
        assertEquals(Mailbox.class, field.addressList.get(1).getClass());
        assertEquals(Mailbox.class, field.addressList.get(2).getClass());
        Mailbox mailbox1 = (Mailbox) field.addressList.get(0);
        Mailbox mailbox2 = (Mailbox) field.addressList.get(1);
        Mailbox mailbox3 = (Mailbox) field.addressList.get(2);
        assertEquals("john", mailbox1.addrSpec.localPart);
        assertNull(mailbox1.displayName);
        assertEquals("jane", mailbox2.addrSpec.localPart);
        assertEquals("Jane Doe", mailbox2.displayName);
        assertEquals("jannie", mailbox3.addrSpec.localPart);
        assertEquals("Jannie Doe", mailbox3.displayName);
    }

    @Test
    public void testFromFieldWithEncodedName() throws ParseException {
        From field = new From();
        new StructuredFieldBodyParser(
                " =?US-ASCII?Q?Keith_Moore?= <moore@example.org>")
                .parseAddressListFieldInto(field);

        assertEquals(1, field.addressList.size());
        assertEquals(Mailbox.class, field.addressList.get(0).getClass());
        Mailbox mailbox1 = (Mailbox) field.addressList.get(0);
        assertEquals("moore", mailbox1.addrSpec.localPart);
        assertEquals("Keith Moore", mailbox1.displayName);
    }

    @Test
    public void testFromFieldWithMultiEncodedName() throws ParseException {
        From field = new From();
        new StructuredFieldBodyParser(
                " =?US-ASCII?Q?Keith_Mo?= =?US-ASCII?Q?ore?= <moore@example.org>")
                .parseAddressListFieldInto(field);

        assertEquals(1, field.addressList.size());
        assertEquals(Mailbox.class, field.addressList.get(0).getClass());
        Mailbox mailbox1 = (Mailbox) field.addressList.get(0);
        assertEquals("moore", mailbox1.addrSpec.localPart);
        assertEquals("Keith Moore", mailbox1.displayName);
    }

    @Test
    public void testToFieldWithNoSpaceAfterGroup() throws ParseException {
        String addressList = "Nightly Monitor Robot:;";
        AddressListField field = new From();
        new StructuredFieldBodyParser(addressList)
                .parseAddressListFieldInto(field);

        Address address;
        Group group;

        assertEquals(1, field.addressList.size());

        address = field.addressList.get(0);
        assertEquals(Group.class, address.getClass());
        group = (Group) address;
        assertEquals("Nightly Monitor Robot", group.displayName);
        assertEquals(0, group.mailboxList.size());
    }

    @Test
    public void testToField() throws ParseException {
        AddressListField field = new To();
        new StructuredFieldBodyParser(" john@example.com")
                .parseAddressListFieldInto(field);

        assertEquals(1, field.addressList.size());
        assertEquals(Mailbox.class, field.addressList.get(0).getClass());
        Mailbox mailbox1 = (Mailbox) field.addressList.get(0);
        assertEquals("john", mailbox1.addrSpec.localPart);
        assertNull(mailbox1.displayName);
    }

    @Test
    public void testToFieldWithGroup() throws ParseException {
        AddressListField field = new To();
        new StructuredFieldBodyParser(" Owners: john@example.com;")
                .parseAddressListFieldInto(field);

        assertEquals(1, field.addressList.size());
        assertEquals(Group.class, field.addressList.get(0).getClass());
        Group group = (Group) field.addressList.get(0);
        assertEquals("Owners", group.displayName);
        assertEquals(1, group.mailboxList.size());
        Mailbox mailbox = group.mailboxList.get(0);
        assertEquals("john", mailbox.addrSpec.localPart);
        assertNull(mailbox.displayName);
    }

    @Test
    public void testToFieldWithGroupWith2Mailbox() throws ParseException {
        AddressListField field = new To();
        new StructuredFieldBodyParser(
                " Owners: john@example.com, Jane Doe <jane@example.com>;")
                .parseAddressListFieldInto(field);

        assertEquals(1, field.addressList.size());
        assertEquals(Group.class, field.addressList.get(0).getClass());
        Group group = (Group) field.addressList.get(0);
        assertEquals("Owners", group.displayName);
        assertEquals(2, group.mailboxList.size());
        Mailbox mailbox = group.mailboxList.get(0);
        assertEquals("john", mailbox.addrSpec.localPart);
        assertNull(mailbox.displayName);
        mailbox = group.mailboxList.get(1);
        assertEquals("jane", mailbox.addrSpec.localPart);
        assertEquals("Jane Doe", mailbox.displayName);
    }

    @Test
    public void testToFieldWithGroupAndMailbox() throws ParseException {
        AddressListField field = new To();
        new StructuredFieldBodyParser(
                " Owners: john@example.com;, Jane Doe <jane@example.com>")
                .parseAddressListFieldInto(field);

        Address address;
        Mailbox mailbox;
        Group group;

        assertEquals(2, field.addressList.size());

        address = field.addressList.get(0);
        assertEquals(Group.class, address.getClass());
        group = (Group) address;
        assertEquals("Owners", group.displayName);
        assertEquals(1, group.mailboxList.size());

        mailbox = group.mailboxList.get(0);
        assertEquals("john", mailbox.addrSpec.localPart);
        assertNull(mailbox.displayName);

        address = field.addressList.get(1);
        assertEquals(Mailbox.class, address.getClass());
        mailbox = (Mailbox) address;
        assertEquals("jane", mailbox.addrSpec.localPart);
        assertEquals("Jane Doe", mailbox.displayName);
    }

    @Test
    public void testToRFC822AddressListsExample() throws ParseException {
        // Galloping Gourmet is unquoted in RFC 822 with no errata, but this
        // is likely wrong.
        String addressList =
                "Gourmets:  Pompous Person <WhoZiWhatZit@Cordon-Bleu>,"
                        + "           Childs@WGBH.Boston, \"Galloping Gourmet\"@"
                        + "           ANT.Down-Under (Australian National Television),"
                        + "           Cheapie@Discount-Liquors;,"
                        + "  Cruisers:  Port@Portugal, Jones@SEA;,"
                        + "    Another@Somewhere.SomeOrg";
        AddressListField field = new To();
        new StructuredFieldBodyParser(addressList)
                .parseAddressListFieldInto(field);

        Address address;
        Mailbox mailbox;
        Group group;

        assertEquals(3, field.addressList.size());

        address = field.addressList.get(0);
        assertEquals(Group.class, address.getClass());
        group = (Group) address;
        assertEquals("Gourmets", group.displayName);
        assertEquals(4, group.mailboxList.size());

        mailbox = group.mailboxList.get(0);
        assertEquals("WhoZiWhatZit", mailbox.addrSpec.localPart);
        assertEquals("Cordon-Bleu",
                ((DotAtomDomainPart) mailbox.addrSpec.domain).domain);
        assertEquals("Pompous Person", mailbox.displayName);

        mailbox = group.mailboxList.get(1);
        assertEquals("Childs", mailbox.addrSpec.localPart);
        assertEquals("WGBH.Boston",
                ((DotAtomDomainPart) mailbox.addrSpec.domain).domain);
        assertNull(mailbox.displayName);

        mailbox = group.mailboxList.get(2);
        assertEquals("Galloping Gourmet", mailbox.addrSpec.localPart);
        assertEquals("ANT.Down-Under",
                ((DotAtomDomainPart) mailbox.addrSpec.domain).domain);
        assertNull(mailbox.displayName);

        mailbox = group.mailboxList.get(3);
        assertEquals("Cheapie", mailbox.addrSpec.localPart);
        assertEquals("Discount-Liquors",
                ((DotAtomDomainPart) mailbox.addrSpec.domain).domain);
        assertNull(mailbox.displayName);

        address = field.addressList.get(1);
        assertEquals(Group.class, address.getClass());
        group = (Group) address;
        assertEquals("Cruisers", group.displayName);
        assertEquals(2, group.mailboxList.size());
    }

}
