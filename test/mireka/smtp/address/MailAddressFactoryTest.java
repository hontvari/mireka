package mireka.smtp.address;

import static org.junit.Assert.assertTrue;

import java.text.ParseException;

import org.junit.Test;

import mireka.ExampleAddress;

public class MailAddressFactoryTest {
    @Test
    public void testCreateRecipientGlobalPostmaster() throws ParseException {
        Recipient recipient = MailAddressFactory.createRecipient("Postmaster");
        assertTrue(recipient instanceof GlobalPostmaster);
    }

    @Test
    public void testCreateRecipientDomainPostmaster() throws ParseException {
        Recipient recipient = MailAddressFactory.createRecipient("Postmaster@example.com");
        assertTrue(recipient instanceof DomainPostmaster);
    }

    @Test
    public void testCreateRecipientGenericRecipient() throws ParseException {
        Recipient recipient = MailAddressFactory.createRecipient("jane@example.com");
        assertTrue(recipient instanceof GenericRecipient);
    }

    @Test
    public void testCreateRecipientAlreadyVerified() {
        Recipient recipient = MailAddressFactory.createRecipientAlreadyVerified("jane@example.com");
        assertTrue(recipient instanceof GenericRecipient);
    }

    @Test
    public void testCreateAddressLiteralRemotePart() {
        RemotePart remotePart =
                MailAddressFactory
                        .createRemotePartFromDisplayableText(ExampleAddress.ADDRESS_LITERAL);
        assertTrue(remotePart instanceof AddressLiteral);
    }

    @Test
    public void testCreateDomainRemotePart() {
        RemotePart remotePart =
                MailAddressFactory.createRemotePartFromDisplayableText(ExampleAddress.EXAMPLE_COM);
        assertTrue(remotePart instanceof DomainPart);
    }

}
