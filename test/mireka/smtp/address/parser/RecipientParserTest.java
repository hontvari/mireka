package mireka.smtp.address.parser;

import static org.junit.Assert.*;
import mireka.smtp.address.parser.RecipientParser;
import mireka.smtp.address.parser.ast.DomainPostmasterRecipientAST;
import mireka.smtp.address.parser.ast.MailboxRecipientAST;
import mireka.smtp.address.parser.ast.RecipientAST;
import mireka.smtp.address.parser.ast.SystemPostmasterRecipientAST;

import org.junit.Test;

public class RecipientParserTest {
    @Test
    public void testSystemPostmaster() throws Exception {
        RecipientAST recipientAST = new RecipientParser("<POSTMASTER>").parse();
        assertTrue(recipientAST instanceof SystemPostmasterRecipientAST);
        assertEquals(
                "POSTMASTER",
                ((SystemPostmasterRecipientAST) recipientAST).postmasterSpelling);
    }

    @Test
    public void testDomainPostmaster() throws Exception {
        RecipientAST recipientAST =
                new RecipientParser("<POSTMASTER@example.com>").parse();
        assertTrue(recipientAST instanceof DomainPostmasterRecipientAST);
        assertEquals(
                "POSTMASTER",
                ((DomainPostmasterRecipientAST) recipientAST).mailboxAST.localPartAST.spelling);
    }

    @Test
    public void testAlmostDomainPostmaster() throws Exception {
        RecipientAST recipientAST =
                new RecipientParser("<Postmaster@[192.0.2.0]>").parse();
        assertTrue(recipientAST instanceof MailboxRecipientAST);
        assertEquals(
                "Postmaster",
                ((MailboxRecipientAST) recipientAST).pathAST.mailboxAST.localPartAST.spelling);
    }

    @Test
    public void testMailboxRecipient() throws Exception {
        RecipientAST recipientAST =
                new RecipientParser("<john@example.com>").parse();
        assertTrue(recipientAST instanceof MailboxRecipientAST);
        assertEquals(
                "john",
                ((MailboxRecipientAST) recipientAST).pathAST.mailboxAST.localPartAST.spelling);
    }
}
