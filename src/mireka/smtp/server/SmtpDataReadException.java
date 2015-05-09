package mireka.smtp.server;

import java.io.IOException;

/**
 * Thrown if a network read error occurs while reading mail data after the SMTP
 * DATA command.
 */
public class SmtpDataReadException extends IOException {
    private static final long serialVersionUID = -8702841343579368798L;
    public final IOException ioExceptionCause;

    public SmtpDataReadException(IOException cause) {
        super(cause);
        this.ioExceptionCause = cause;
    }

}
