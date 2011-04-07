package mireka.address;

import static org.junit.Assert.*;

import java.text.ParseException;

import mireka.ExampleAddress;

import org.junit.Test;

public class MailAddressFactoryTest {
    private MailAddressFactory factory = new MailAddressFactory();

    @Test
    public void testCreateRecipientGlobalPostmaster() throws ParseException {
        Recipient recipient = factory.createRecipient("Postmaster");
        assertTrue(recipient instanceof GlobalPostmaster);
    }

    @Test
    public void testCreateRecipientDomainPostmaster() throws ParseException {
        Recipient recipient = factory.createRecipient("Postmaster@example.com");
        assertTrue(recipient instanceof DomainPostmaster);
    }

    @Test
    public void testCreateRecipientGenericRecipient() throws ParseException {
        Recipient recipient = factory.createRecipient("jane@example.com");
        assertTrue(recipient instanceof GenericRecipient);
    }

    @Test
    public void testCreateRecipientAlreadyVerified() {
        Recipient recipient =
                factory.createRecipientAlreadyVerified("jane@example.com");
        assertTrue(recipient instanceof GenericRecipient);
    }

    @Test
    public void testCreateAddressLiteralRemotePart() {
        RemotePart remotePart =
                factory.createRemotePartFromDisplayableText(ExampleAddress.ADDRESS_LITERAL);
        assertTrue(remotePart instanceof AddressLiteral);
    }

    @Test
    public void testCreateDomainRemotePart() {
        RemotePart remotePart =
                factory.createRemotePartFromDisplayableText(ExampleAddress.EXAMPLE_COM);
        assertTrue(remotePart instanceof DomainPart);
    }

}
