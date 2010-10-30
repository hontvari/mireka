package mireka.filter.proxy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import mireka.MailData;
import mireka.filter.AbstractDataRecipientFilter;
import mireka.filter.DataRecipientFilterAdapter;
import mireka.filter.Destination;
import mireka.filter.Filter;
import mireka.filter.FilterType;
import mireka.filter.MailTransaction;
import mireka.filter.RecipientContext;
import mireka.filter.local.table.Relay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

/**
 * RelayMailTransaction relays each step of the mail transaction in "realtime".
 * Different recipients may be relayed to different backend servers. This filter
 * handles only recipients whose destination is a {@link Relay}. The algorithm
 * is the same as in Baton. The backend server is only connected on the first
 * RCPT SMTP statement. This means that with proper configuration the backend
 * server is not connected at all if no recipient were accepted. Moreover, in
 * this way the decision of which which server to use may depend on the
 * recipient address. The delaying is useful because most mail transactions are
 * SPAM, and they are aborted after the first RCPT TO command is received and
 * rejected.
 * <p>
 * Note: it does not verify any recipient. In order to deliver the message some
 * other filter must verify and accept the recipients.
 * 
 * @see <a href="http://code.google.com/p/baton/">Baton SMTP proxy</a>
 */
public class RelayMailTransaction implements FilterType {

    @Override
    public Filter createInstance(MailTransaction mailTransaction) {
        FilterImpl filterInstance = new FilterImpl(mailTransaction);
        return new DataRecipientFilterAdapter(filterInstance, mailTransaction);
    }

    static class FilterImpl extends AbstractDataRecipientFilter {
        private Logger logger = LoggerFactory.getLogger(FilterImpl.class);
        private Map<BackendServer, BackendClient> serverClientMap =
                new HashMap<BackendServer, BackendClient>();
        private String from;

        protected FilterImpl(MailTransaction mailTransaction) {
            super(mailTransaction);
        }

        @Override
        public void from(String from) {
            this.from = from;
        }

        @Override
        public void recipient(RecipientContext recipientContext)
                throws RejectException {
            Destination destination = recipientContext.getDestination();
            if (!(destination instanceof Relay))
                return;
            relayRecipientToServer(recipientContext, (Relay) destination);
        }

        private void relayRecipientToServer(RecipientContext recipientContext,
                Relay destination) throws RejectException,
                BackendRejectException {
            BackendServer server = destination.getBackendServer();
            BackendClient client = getOrCreateClient(server);
            // if there was an IO error, or the "from" was rejected, then
            // reject this recipient too (with the same exception?)
            client.recipient(recipientContext.recipient);
        }

        private BackendClient getOrCreateClient(BackendServer server) {
            BackendClient client = serverClientMap.get(server);
            if (client == null) {
                client = new BackendClient(server);
                serverClientMap.put(server, client);
                try {
                    client.connect();
                    client.from(from);
                } catch (RejectException e) {
                    logger.debug("Connection to backend server failed, "
                            + "failed status is memorized, continuing...");
                }
            }
            return client;
        }

        @Override
        public void data(MailData data) throws TooMuchDataException,
                RejectException, IOException {
            boolean hasAcceptedRecipient = false;
            for (BackendClient client : serverClientMap.values()) {
                if (client.hasAcceptedRecipient()) {
                    hasAcceptedRecipient = true;
                    client.data(data.getInputStream());
                }
            }
            if (!hasAcceptedRecipient) {
                throw new RejectException(554, "No valid recipients");
            }
        }

        @Override
        public void done() {
            for (BackendClient client : serverClientMap.values()) {
                client.quit();
            }
        }
    }

}
