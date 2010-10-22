package mireka.filter.proxy;

import java.io.IOException;

import mireka.MailData;
import mireka.filter.AbstractDataRecipientFilter;
import mireka.filter.DataRecipientFilterAdapter;
import mireka.filter.Filter;
import mireka.filter.FilterType;
import mireka.filter.MailTransaction;
import mireka.filter.RecipientContext;

import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

/**
 * Relays each step of the mail transaction in "realtime". The algorithm is the
 * same as in Baton. The backend server is only connected on the first RCPT SMTP
 * statement. This means that with proper configuration the backed server is not
 * connected at all if no recipient were accepted.
 * <p>
 * Note: it does not verify any recipient. In order to deliver the message some
 * other filter must accept the recipients.
 * 
 * @see <a href="http://code.google.com/p/baton/">Baton SMTP proxy</a>
 */
public class RelayMailTransaction implements FilterType {
    private BackendServer backendServer;

    @Override
    public Filter createInstance(MailTransaction mailTransaction) {
        LazyBackendConnector lazyBackendConnector =
                new LazyBackendConnector(backendServer);
        LazyClient lazyClient = new LazyClient(lazyBackendConnector);
        FilterImpl filterInstance = new FilterImpl(mailTransaction, lazyClient);
        return new DataRecipientFilterAdapter(filterInstance, mailTransaction);
    }

    /**
     * @category GETSET
     */
    public BackendServer getBackendServer() {
        return backendServer;
    }

    /**
     * @category GETSET
     */
    public void setBackendServer(BackendServer backendServer) {
        this.backendServer = backendServer;
    }

    private class FilterImpl extends AbstractDataRecipientFilter {
        private LazyClient backend;

        protected FilterImpl(MailTransaction mailTransaction, LazyClient backend) {
            super(mailTransaction);
            this.backend = backend;
        }

        @Override
        public void from(String from) {
            backend.from(from);
        }

        @Override
        public void recipient(RecipientContext recipientContext) throws RejectException {
            backend.recipient(recipientContext.recipient);
        }

        @Override
        public void data(MailData data) throws TooMuchDataException,
                RejectException, IOException {
            backend.data(data.getInputStream());
        }

        @Override
        public void done() {
            backend.done();
        }
    }
}
