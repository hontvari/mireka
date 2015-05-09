package mireka.smtp.server;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import mireka.Version;

import org.subethamail.smtp.MessageHandlerFactory;

/**
 * SMTPServer listens on typically port 25 or 587 and accepts connections from
 * SMTP clients and accepts mails for sending forward or for final delivery.
 * This class is a minor extension to the SubEthaSMTP SMTPServer class. When a
 * mail transaction starts it requests a MessageHandler from the specified
 * MessageHandlerFactory and passes all information in preprocessed form to that
 * object.
 * 
 * @see org.subethamail.smtp.server.SMTPServer
 */
public class SMTPServer extends org.subethamail.smtp.server.SMTPServer {

    public SMTPServer(MessageHandlerFactory handlerFactory) {
        super(handlerFactory);
        setSoftwareName("Mireka " + Version.getVersion());
    }

    @Override
    @PostConstruct
    public void start() {
        super.start();
    }

    @Override
    @PreDestroy
    public void stop() {
        super.stop();
    }

    public void setBindAddress(String bindAddress) {
        try {
            setBindAddress(InetAddress.getByName(bindAddress));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
