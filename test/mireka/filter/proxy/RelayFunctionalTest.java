package mireka.filter.proxy;

import static org.junit.Assert.*;

import java.io.IOException;

import mireka.ExampleMaildataFile;
import mireka.destination.DestinationProcessorFilter;
import mireka.filter.FilterChain;
import mireka.filter.local.LookupDestinationFilter;
import mireka.filter.local.table.AnyRecipient;
import mireka.filter.local.table.RecipientSpecificationDestinationPair;
import mireka.smtp.client.BackendServer;
import mireka.smtp.client.ClientFactory;
import mireka.smtp.server.MessageHandlerFactoryImpl;
import mireka.smtp.server.SMTPServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.subethamail.smtp.client.SmartClient;
import org.subethamail.smtp.command.QuitCommand;
import org.subethamail.smtp.server.Session;
import org.subethamail.wiser.Wiser;

public class RelayFunctionalTest {
    private SMTPServer relayServer;
    private Wiser wiser;
    private volatile boolean quitReceived;

    @Before
    public void init() {
        setupWiser();
        setupRelay();
    }

    private void setupWiser() {
        wiser = new Wiser(2525);
        wiser.getServer().getCommandHandler()
                .addCommand(new ObservableQuitCommand());
        wiser.start();
    }

    private void setupRelay() {
        ClientFactory clientFactory = new ClientFactory();
        clientFactory.setHelo("test");

        BackendServer backendServer = new BackendServer();
        backendServer.setPort(2525);
        backendServer.setClientFactory(clientFactory);

        RelayDestination destination = new RelayDestination();
        destination.setBackendServer(backendServer);

        RecipientSpecificationDestinationPair recipientDestinationMapper =
                new RecipientSpecificationDestinationPair();
        recipientDestinationMapper
                .addRecipientSpecification(new AnyRecipient());
        recipientDestinationMapper.setDestination(destination);

        LookupDestinationFilter lookupDestinationFilter =
                new LookupDestinationFilter();
        lookupDestinationFilter
                .setRecipientDestinationMapper(recipientDestinationMapper);

        DestinationProcessorFilter destinationProcessorFilter =
                new DestinationProcessorFilter();

        FilterChain filters = new FilterChain();
        filters.addFilter(lookupDestinationFilter);
        filters.addFilter(destinationProcessorFilter);

        MessageHandlerFactoryImpl handlerFactory =
                new MessageHandlerFactoryImpl();
        handlerFactory.setFilters(filters);
        relayServer = new SMTPServer(handlerFactory);
        relayServer.setPort(2524);
        relayServer.start();

    }

    @After
    public void cleanup() {
        relayServer.stop();
        wiser.stop();
    }

    @Test
    public void testQuit() throws Exception {
        SmartClient smartClient =
                new SmartClient("localhost", 2524, "localhost");
        smartClient.from("");
        smartClient.to("postmaster@example.org");
        smartClient.dataStart();
        byte[] bytes = ExampleMaildataFile.simple().bytes;
        smartClient.dataWrite(bytes, bytes.length);
        smartClient.dataEnd();
        smartClient.quit();

        assertTrue(quitReceived);
        assertEquals(1, wiser.getMessages().size());
    }

    private class ObservableQuitCommand extends QuitCommand {

        @Override
        public void execute(String commandString, Session sess)
                throws IOException {
            quitReceived = true;
            super.execute(commandString, sess);
        }
    }
}
