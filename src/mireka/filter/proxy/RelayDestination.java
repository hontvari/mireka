package mireka.filter.proxy;

import java.io.IOException;
import java.io.InputStream;

import mireka.address.ReversePath;
import mireka.destination.Session;
import mireka.destination.SessionDestination;
import mireka.filter.RecipientContext;
import mireka.smtp.EnhancedStatus;
import mireka.smtp.RejectExceptionExt;
import mireka.smtp.client.BackendServer;
import mireka.transmission.Mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.RejectException;

/**
 * RelayDestination relays each step of the mail transaction in "realtime" to a
 * gateway. Different recipients may be relayed to different backend servers.
 * This filter handles only recipients whose destination is a
 * {@link RelayDestination}. The algorithm is the same as in Baton. The backend
 * server is only connected on the first RCPT SMTP statement. This means that
 * with proper configuration the backend server is not connected at all if no
 * recipient were accepted. Moreover, in this way the decision of which which
 * server to use may depend on the recipient address. The delaying is useful
 * because most mail transactions are SPAM, and they are aborted after the first
 * RCPT TO command is received and rejected.
 * <p>
 * Note: it does not verify any recipient. In order to deliver the message some
 * other filter must verify and accept the recipients.
 * 
 * @see <a href="http://code.google.com/p/baton/">Baton SMTP proxy</a>
 */
public class RelayDestination implements SessionDestination {
    private Logger logger = LoggerFactory.getLogger(RelayDestination.class);
    private BackendServer backendServer;

    @Override
    public Session createSession() {
        return new SessionImpl();
    }

    /**
     * @x.category GETSET
     */
    public BackendServer getBackendServer() {
        return backendServer;
    }

    /**
     * @x.category GETSET
     */
    public void setBackendServer(BackendServer backendServer) {
        this.backendServer = backendServer;
    }

    @Override
    public String toString() {
        return "RelayDestination [backendServer=" + backendServer.getHost()
                + ":" + backendServer.getPort() + "]";
    }

    private class SessionImpl implements Session {
        private BackendClient client;
        private ReversePath from;

        @Override
        public void from(ReversePath from) throws RejectExceptionExt {
            this.from = from;
        }

        @Override
        public void recipient(RecipientContext recipientContext)
                throws RejectExceptionExt {
            initClient();
            // if there was an IO error, or the "from" was rejected, then
            // this recipient will be rejected too
            client.recipient(recipientContext.recipient);
        }

        private void initClient() {
            if (client != null)
                return;

            client = new BackendClient(backendServer);
            try {
                client.connect();
                client.from(from.getSmtpText());
            } catch (RejectException e) {
                logger.debug("Connection to backend server failed, "
                        + "failed status is memorized, continuing...");
            }
        }

        /**
         * Relays maildata to the backend server.
         */
        @Override
        public void data(Mail mail) throws RejectExceptionExt {
            if (!client.hasAcceptedRecipient())
                return;
            try (InputStream dataStream = mail.maildata.getInputStream()) {
                try {
                    client.data(dataStream);
                } catch (IOException e) {
                    logger.error("Sending data to backend failed", e);
                    throw new RejectExceptionExt(EnhancedStatus.BAD_CONNECTION);
                }
            } catch (IOException e) {
                logger.error("Cannot read maildata output", e);
                // Some local reason. Most likely disk full.
                throw new RejectExceptionExt(EnhancedStatus.MAIL_SYSTEM_FULL);
            }
        }

        @Override
        public void done() {
            if (client != null)
                client.quit();
        }
    }
}
