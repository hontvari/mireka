package mireka.maildata.io;

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
 * command handling code.
 */
public class MaildataFileReadException extends RuntimeException {
    private static final long serialVersionUID = 3949930255421607723L;

    public MaildataFileReadException(Throwable cause) {
        super(cause);
    }

}
