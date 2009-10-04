package mireka.filter.builtin.proxy;

import java.io.IOException;
import java.io.InputStream;

import javax.enterprise.inject.Initializer;

import mireka.mailaddress.Recipient;

import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.client.SMTPException;
import org.subethamail.smtp.client.SmartClient;

public class LazyClient {

    private final LazyBackendConnector backend;
    private String from;

    @Initializer
    public LazyClient(LazyBackendConnector backend) {
        this.backend = backend;
    }

    public void from(String from) {
        this.from = from;
    }

    public void recipient(Recipient recipient) throws RejectException,
            BackendRejectException {
        connectToBackend();
        sendFromIfNotYetSent();
        sendRecipient(recipient);
    }

    private void connectToBackend() throws BackendRejectException,
            RejectException {
        try {
            backend.connection();
        } catch (SMTPException e) {
            throw new BackendRejectException(e,
                    " - Backend rejected connection");
        } catch (IOException e) {
            throw new RejectException(451, e.getMessage());
        }
    }

    private void sendFromIfNotYetSent() throws BackendRejectException,
            RejectException {
        try {
            if (!backend.connection().sentFrom())
                backend.connection().from(from);
        } catch (SMTPException e) {
            throw new BackendRejectException(e, " - Backend rejected sender");
        } catch (IOException e) {
            throw new RejectException(451, e.getMessage());
        }
    }

    private void sendRecipient(Recipient recipient)
            throws BackendRejectException, RejectException {
        String destinationMailbox = null;
        try {
            destinationMailbox = recipient.sourceRouteStripped();
            backend.connection().to(destinationMailbox);
        } catch (SMTPException e) {
            throw new BackendRejectException(e, " - Backend rejected recipient");
        } catch (IOException e) {
            throw new RejectException(451, e.getMessage());
        }
    }

    public void data(InputStream data) throws RejectException,
            TooMuchDataException, IOException {
        this.sendDataStart();
        this.sendDataStream(data);
        this.sendDataEnd();
    }

    /**
     * Start the DATA command on backend server.
     */
    private void sendDataStart() throws IOException, BackendRejectException {
        SmartClient backendConnection = backend.connection();
        try {
            backendConnection.dataStart();
        } catch (SMTPException e) {
            throw new BackendRejectException(e, " - Backend rejected DATA");
        }
    }

    private void sendDataStream(InputStream data) throws IOException {
        SmartClient backendConnection = backend.connection();
        byte[] buffer = new byte[8192];
        int numRead;
        while ((numRead = data.read(buffer)) > 0) {
            backendConnection.dataWrite(buffer, numRead);
        }
    }

    /**
     * Complete the data session on all connected targets. Shut down and remove
     * targets that fail.
     */
    private void sendDataEnd() throws IOException, BackendRejectException {
        SmartClient backendConnection = backend.connection();

        try {
            backendConnection.dataEnd();
        } catch (SMTPException e) {
            throw new BackendRejectException(e,
                    " - Backend server rejected at end of data");
        }
    }

    public void done() {
        backend.done();
    }
}
