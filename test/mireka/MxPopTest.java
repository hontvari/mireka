package mireka;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

import mireka.destination.DestinationProcessorFilter;
import mireka.filter.local.LookupDestinationFilter;
import mireka.filterchain.Filters;
import mireka.login.GlobalUser;
import mireka.login.GlobalUsers;
import mireka.login.GlobalUsersLoginSpecification;
import mireka.login.GlobalUsersMaildropDestinationMapper;
import mireka.login.GlobalUsersPrincipalMaildropTable;
import mireka.pop.PopServer;
import mireka.pop.store.MaildropRepository;
import mireka.smtp.server.MessageHandlerFactoryImpl;
import mireka.smtp.server.SMTPServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.subethamail.smtp.client.SMTPException;
import org.subethamail.smtp.client.SmartClient;

public class MxPopTest extends TempDirectory {

    private static final int PORT_SMTP = 8025;
    private static final int PORT_POP = 8026;
    private GlobalUsers users;
    private MaildropRepository maildropRepository;
    private PopServer popServer;
    private SMTPServer smtpServer;

    @Test
    public void testReceiveRetrieve() throws IOException, SMTPException,
            MessagingException {

        sendMail();

        retrieveMail();
    }

    private void sendMail() throws UnknownHostException, IOException,
            SMTPException {
        SmartClient client = new SmartClient("localhost", PORT_SMTP,
                "SmartClient");
        client.from("jane@example.com");
        client.to("john@example.com");
        client.dataStart();
        byte[] exampleMail = ExampleMailData.simple().bytes;
        client.dataWrite(exampleMail, exampleMail.length);
        client.dataEnd();
        client.quit();
    }

    private void retrieveMail() throws NoSuchProviderException,
            MessagingException, IOException {
        Properties properties = new Properties();
        Session session = Session.getInstance(properties);
        Store store = session.getStore(new URLName(
                "pop3://john:secret@localhost:" + PORT_POP + "/INBOX"));
        store.connect();
        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_WRITE);
        Message[] messages = folder.getMessages();
        assertEquals(1, messages.length);
        Message message = messages[0];
        assertEquals("Hello World!\r\n", message.getContent());
        message.setFlag(Flags.Flag.DELETED, true);
        folder.close(true);
        store.close();
    }

    @Before
    public void setup() {
        initCommonConfiguration();

        smtpServer = createSmtpServer();
        smtpServer.start();

        popServer = createPopServer();
        popServer.start();

    }

    private void initCommonConfiguration() {
        users = new GlobalUsers();
        GlobalUser user = new GlobalUser();
        user.setUsername("john");
        user.setPassword("secret");
        users.addUser(user);

        maildropRepository = new MaildropRepository();
        maildropRepository.setDir(directory.getPath());
    }

    private SMTPServer createSmtpServer() {
        GlobalUsersMaildropDestinationMapper recipientDestinationMapper = new GlobalUsersMaildropDestinationMapper();
        recipientDestinationMapper.setUsers(users);
        recipientDestinationMapper.setMaildropRepository(maildropRepository);

        LookupDestinationFilter lookupDestinationFilter = new LookupDestinationFilter();
        lookupDestinationFilter
                .setRecipientDestinationMapper(recipientDestinationMapper);

        Filters filters = new Filters();
        filters.addFilter(lookupDestinationFilter);
        filters.addFilter(new DestinationProcessorFilter());

        MessageHandlerFactoryImpl handlerFactory = new MessageHandlerFactoryImpl();
        handlerFactory.setFilters(filters);

        SMTPServer smtpServer = new SMTPServer(handlerFactory);
        smtpServer.setPort(PORT_SMTP);
        return smtpServer;
    }

    private PopServer createPopServer() {
        GlobalUsersLoginSpecification loginSpecification = new GlobalUsersLoginSpecification();
        loginSpecification.setUsers(users);

        GlobalUsersPrincipalMaildropTable principalMaildropTable = new GlobalUsersPrincipalMaildropTable();

        PopServer popServer = new PopServer();
        popServer.setMaildropRepository(maildropRepository);
        popServer.setLoginSpecification(loginSpecification);
        popServer.setPrincipalMaildropTable(principalMaildropTable);
        popServer.setPort(PORT_POP);
        return popServer;
    }

    @After
    public void tearDown() {
        popServer.shutdown();
        smtpServer.stop();
    }
}
