package mireka.filter.local.table;

import static org.junit.Assert.*;
import mireka.address.MailAddressFactory;
import mockit.Expectations;

import org.junit.Test;

public class RecipientSpecificationFactoryTest {

    @Test
    public final void testCreateGlobalPostmaster() {
        RecipientSpecificationFactory factory =
                new RecipientSpecificationFactory();
        assertTrue(factory.create("Postmaster") instanceof GlobalPostmasterSpecification);
    }

    @Test
    public final void testCreateDomainPostmaster(
            final MailAddressFactory mailAddressFactory) {
        RecipientSpecificationFactory factory =
                new RecipientSpecificationFactory();
        new Expectations() {
            {
                mailAddressFactory.createRemotePart("example.com");
            }

        };
        assertTrue(factory.create("Postmaster@example.com") instanceof DomainPostmasterSpecification);
    }

    @Test
    public final void testCreateUsualMailbox(
            final MailAddressFactory mailAddressFactory) {
        RecipientSpecificationFactory factory =
                new RecipientSpecificationFactory();
        new Expectations() {
            {
                new CaseInsensitiveLocalPartSpecification("jane");
                mailAddressFactory.createRemotePart("example.com");
            }
        };
        assertTrue(factory.create("jane@example.com") instanceof LocalRemoteCombinedRecipientSpecification);
    }
}
