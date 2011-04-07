package mireka.filter.local.table;

import static org.junit.Assert.*;

import org.junit.Test;

public class RecipientSpecificationFactoryTest {

    @Test
    public final void testCreateGlobalPostmaster() {
        RecipientSpecificationFactory factory =
                new RecipientSpecificationFactory();
        assertTrue(factory.create("Postmaster") instanceof GlobalPostmasterSpecification);
    }

    @Test
    public final void testCreateDomainPostmaster() {
        RecipientSpecificationFactory factory =
                new RecipientSpecificationFactory();
        assertTrue(factory.create("Postmaster@example.com") instanceof DomainPostmasterSpecification);
    }

    @Test
    public final void testCreateUsualMailbox() {
        RecipientSpecificationFactory factory =
                new RecipientSpecificationFactory();
        assertTrue(factory.create("jane@example.com") instanceof LocalRemoteCombinedRecipientSpecification);
    }
}
