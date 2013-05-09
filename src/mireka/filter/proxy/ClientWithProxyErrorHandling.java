package mireka.filter.proxy;

import java.io.IOException;
import java.io.InputStream;

import mireka.address.Recipient;
import mireka.smtp.SendException;
import mireka.smtp.client.BackendServer;
import mireka.smtp.client.SmtpClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.client.SMTPException;
import org.subethamail.smtp.client.SmartClient;

/**
 * ClientWithProxyErrorHandling decorates a SmartClient with exception handling
 * suitable for a proxy: it converts backend error responses and IO errors to
 * SMTP errors which are valid from the viewpoint of the original sender.
 */
class ClientWithProxyErrorHandling {
    private final Logger logger = LoggerFactory
            .getLogger(ClientWithProxyErrorHandling.class);

    private final BackendServer backend;
    private final SmartClient smartClient;

    public ClientWithProxyErrorHandling(BackendServer backend)
            throws BackendRejectException, RejectException {
        this.backend = backend;
        this.smartClient = connect();
    }

    private SmartClient connect() throws BackendRejectException,
            RejectException {
        SmtpClient client;
        try {
            client = backend.createClient();
        } catch (SendException e) {
            throw new RejectException(e.errorStatus().getSmtpReplyCode(), e.errorStatus().getMessage());
        }
        try {
            client.connect();
        } catch (SMTPException e) {
            throw new BackendRejectException(e,
                    " - Backend rejected connection");
        } catch (IOException e) {
            logger.error(
                    "Error while communicating with " + backend.toString(), e);
            throw new RejectException(451, "Local error in processing.");
        }
        return client;
    }

    public void from(String from) throws BackendRejectException,
            RejectException {
        try {
            smartClient.from(from);
        } catch (SMTPException e) {
            throw new BackendRejectException(e, " - Backend rejected sender");
        } catch (IOException e) {
            throw new RejectException(451, e.getMessage());
        }
    }

    public void recipient(Recipient recipient) throws RejectException,
            BackendRejectException {
        String destinationMailbox = null;
        try {
            destinationMailbox = recipient.sourceRouteStripped();
            smartClient.to(destinationMailbox);
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
    private void sendDataStart() throws RejectException, BackendRejectException {
        try {
            smartClient.dataStart();
        } catch (SMTPException e) {
            throw new BackendRejectException(e, " - Backend rejected DATA");
        } catch (IOException e) {
            throw new RejectException(451, e.getMessage());
        }
    }

    private void sendDataStream(InputStream data) throws IOException,
            RejectException {
        byte[] buffer = new byte[8192];
        int numRead;
        while ((numRead = data.read(buffer)) > 0) {
            try {
                smartClient.dataWrite(buffer, numRead);
            } catch (IOException e) {
                throw new RejectException(451, e.getMessage());
            }
        }
    }

    /**
     * Complete the data session on all connected targets. Shut down and remove
     * targets that fail.
     */
    private void sendDataEnd() throws RejectException, BackendRejectException {
        try {
            smartClient.dataEnd();
        } catch (SMTPException e) {
            throw new BackendRejectException(e,
                    " - Backend server rejected at end of data");
        } catch (IOException e) {
            throw new RejectException(451, e.getMessage());
        }
    }

    public void quit() {
        smartClient.quit();
    }
}
