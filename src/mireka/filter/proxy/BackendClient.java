package mireka.filter.proxy;

import java.io.IOException;
import java.io.InputStream;

import mireka.address.Recipient;

import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

/**
 * BackendClient decorates a {@link ClientWithProxyErrorHandling} instance with
 * memorization of connection state, i.e. previous exceptions.
 */
public class BackendClient {

    private final BackendServer backendServer;
    private ClientWithProxyErrorHandling client;
    private RejectException exceptionAffectingConnection;
    private boolean hasAcceptedRecipient;

    public BackendClient(BackendServer backendServer) {
        this.backendServer = backendServer;
    }

    public void connect() throws BackendRejectException, RejectException {
        try {
            client = new ClientWithProxyErrorHandling(backendServer);
        } catch (RejectException e) {
            exceptionAffectingConnection = e;
            throw e;
        }
    }

    public void from(String from) throws BackendRejectException,
            RejectException {
        if (exceptionAffectingConnection != null)
            throw exceptionAffectingConnection;
        try {
            client.from(from);
        } catch (RejectException e) {
            exceptionAffectingConnection = e;
            throw e;
        }
    }

    public void recipient(Recipient recipient) throws RejectException,
            BackendRejectException {
        if (exceptionAffectingConnection != null)
            throw exceptionAffectingConnection;
        try {
            client.recipient(recipient);
            hasAcceptedRecipient = true;
        } catch (RejectException e) {
            exceptionAffectingConnection = e;
            throw e;
        }
    }

    public boolean hasAcceptedRecipient() {
        return hasAcceptedRecipient;
    }

    /**
     * Sends data to backend server. It must not be called if no recipient was
     * accepted previously. It does not memorize an exception coming from the
     * backed server, but DATA is expected to be the last command anyway (except
     * QUIT).
     */
    public void data(InputStream data) throws RejectException,
            TooMuchDataException, IOException {
        if (!hasAcceptedRecipient)
            throw new IllegalStateException("DATA command must not be sent if "
                    + "there is no valid recipient");
        if (exceptionAffectingConnection != null)
            throw new IllegalStateException();
        client.data(data);
    }

    /**
     * Sends QUIT command and closes the connection. This is allowed in any
     * state.
     */
    public void quit() {
        if (client == null)
            return;
        client.quit();
    }

}
