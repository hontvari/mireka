package mireka.server;

import static org.junit.Assert.*;

import java.io.IOException;

import mireka.ArrayEndsWith;
import mireka.ExampleAddress;
import mireka.ExampleMailData;
import mireka.destination.DestinationProcessorFilter;
import mireka.filter.local.AcceptAllRecipient;
import mireka.filter.local.LookupDestinationFilter;
import mireka.filter.local.table.RecipientSpecificationDestinationPair;
import mireka.filter.local.table.RecipientSpecificationFactory;
import mireka.filter.proxy.BackendServer;
import mireka.filter.proxy.RelayDestination;
import mireka.filterchain.Filters;
import mireka.smtp.ClientFactory;
import mireka.smtp.server.MessageHandlerFactoryImpl;
import mireka.smtp.server.SMTPServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.subethamail.smtp.client.SMTPException;
import org.subethamail.smtp.client.SmartClient;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

public class ClientServerRelayTest {
    private SMTPServer smtpServer;
    private Wiser wiser;

    @Before
    public void setup() {
        setupSmtpService();
        setupWiser();
    }

    private void setupSmtpService() {
        Filters filters = createFilters();

        MessageHandlerFactoryImpl handlerFactoryImpl =
                new MessageHandlerFactoryImpl();
        handlerFactoryImpl.setFilters(filters);
        smtpServer = new SMTPServer(handlerFactoryImpl);
        smtpServer.setPort(8025);
        smtpServer.start();
    }

    private Filters createFilters() {
        Filters filters = new Filters();

        ClientFactory client = new ClientFactory();
        client.setHelo("relay.localdomain");
        BackendServer backendServer = new BackendServer();
        backendServer.setHost("localhost");
        backendServer.setPort(8026);
        backendServer.setClientFactory(client);
        RelayDestination relayDestination = new RelayDestination();
        relayDestination.setBackendServer(backendServer);

        RecipientSpecificationDestinationPair recipientDestinationMapper =
                new RecipientSpecificationDestinationPair();
        recipientDestinationMapper
                .addRecipientSpecification(new RecipientSpecificationFactory()
                        .create(ExampleAddress.JANE));
        recipientDestinationMapper.setDestination(relayDestination);
        LookupDestinationFilter lookupDestinationFilter = new LookupDestinationFilter();
        lookupDestinationFilter
                .setRecipientDestinationMapper(recipientDestinationMapper);
        filters.addFilter(lookupDestinationFilter);

        filters.addFilter(new AcceptAllRecipient());

        DestinationProcessorFilter destinationProcessFilter =
                new DestinationProcessorFilter();
        filters.addFilter(destinationProcessFilter);
        return filters;
    }

    private void setupWiser() {
        wiser = new Wiser(8026);
        wiser.start();
    }

    @Test
    public void testReceivingAndSending() throws IOException, SMTPException,
            IOException {
        SmartClient client = new SmartClient("localhost", 8025, "SmartClient");
        client.from("john@example.com");
        client.to("jane@example.com");
        client.dataStart();
        byte[] exampleMail = ExampleMailData.simple().bytes;
        client.dataWrite(exampleMail, exampleMail.length);
        client.dataEnd();
        client.quit();

        WiserMessage message = wiser.getMessages().get(0);
        assertThat(message.getData(), new ArrayEndsWith(exampleMail));
    }

    @After
    public void cleanup() {
        wiser.stop();
        smtpServer.stop();
    }
}
