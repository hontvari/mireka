package mireka.maildata.io;

import java.io.IOException;

import mireka.ConfigurationException;

/**
 * Thrown if an IO exception occurs while reading a MaildataFile. It is a
 * unchecked exception, because
 * <ul>
 * <li>it is almost unexpected under normal circumstances. It can only happen if
 * the /tmp directory is a network mounted directory or in case of a hardware
 * failure. The storage device of these files are memory or a temporary file
 * which is recently written to disk, its content is probably in the file system
 * cache.
 * <li>it is too low level for the classes which directly use a MaildataFile and
 * they cannot handle it.
 * <li>it goes through a very large number of classes
 * </ul>
 * 
 * It is handled similarly to late configuration errors, indicated by
 * {@link ConfigurationException} exceptions, within the top level SMTP DATA
 * command handling code. It is considered as a local system error, and not a
 * program error.
 * 
 * Remark: If in the future a MaildataFile become directly connected to the TCP
 * connection from the client, this exception still does not hide the problem,
 * because the original IOException is stored in the {@link #ioExceptionCause}
 * field, which is extracted by the SMTP handler code. But in that case a
 * checked exception (namely IOException) should replace this class.
 */
public class MaildataFileReadException extends RuntimeException {
    private static final long serialVersionUID = 3949930255421607723L;
    public final IOException ioExceptionCause;

    public MaildataFileReadException(IOException cause) {
        super(cause);
        this.ioExceptionCause = cause;
    }

}
