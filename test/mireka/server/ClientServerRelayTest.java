package mireka.server;

import java.io.IOException;

import mireka.ArrayEndsWith;
import mireka.ExampleMails;
import mireka.filter.builtin.local.AcceptAllRecipient;
import mireka.filter.builtin.proxy.BackendClient;
import mireka.filter.builtin.proxy.BackendServer;
import mireka.filter.builtin.proxy.RelayMailTransaction;
import mireka.filterchain.Filters;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.subethamail.smtp.client.SMTPException;
import org.subethamail.smtp.client.SmartClient;
import org.subethamail.smtp.server.SMTPServer;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import static org.junit.Assert.assertThat;

public class ClientServerRelayTest {
    private SMTPService smtpService;
    private Wiser wiser;

    @Before
    public void setup() {
        setupSmtpService();
        setupWiser();
    }

    private void setupSmtpService() {
        Filters filters = new Filters();
        filters.addFilter(new AcceptAllRecipient());

        BackendClient client = new BackendClient();
        BackendServer backendServer = new BackendServer();
        backendServer.setHost("localhost");
        backendServer.setPort(8026);
        backendServer.setClient(client);
        RelayMailTransaction relayFilter = new RelayMailTransaction();
        relayFilter.setBackendServer(backendServer);
        filters.addFilter(relayFilter);

        MessageHandlerFactoryImpl handlerFactoryImpl =
                new MessageHandlerFactoryImpl();
        handlerFactoryImpl.setFilters(filters);
        SMTPServer smtpServer = new SMTPServer(handlerFactoryImpl);
        smtpServer.setPort(8025);
        smtpService = new SMTPService();
        smtpService.setSmtpServer(smtpServer);
        smtpService.start();
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
        byte[] exampleMail = ExampleMails.simple().bytes;
        client.dataWrite(exampleMail, exampleMail.length);
        client.dataEnd();
        client.quit();

        WiserMessage message = wiser.getMessages().get(0);
        assertThat(message.getData(), new ArrayEndsWith(exampleMail));
    }

    @After
    public void cleanup() {
        wiser.stop();
        smtpService.stop();
    }
}
