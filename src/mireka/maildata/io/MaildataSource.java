package mireka.maildata.io;

/**
 * MaildataFile represents a byte stream which can be read multiple times, it is
 * used to store Mail Data. Mail data is the material transmitted after the SMTP
 * DATA command is accepted and before the end of data indication is
 * transmitted.
 * 
 * Idea: Maybe this class can be converted to an InputStream with guaranteed reset capability.  
 * 
 * @see <a href="http://tools.ietf.org/html/rfc5321#section-2.3.9">RFC 5321
 *      2.3.9. Message Content and Mail Data</a>
 */
public interface MaildataSource extends AutoCloseable {

    /**
     * Returns the data stream positioned and limited to the supplied Range. The caller must close
     * the returned stream.
     */
    MaildataInputStream getInputStream(Range range) throws MaildataReadException;

    /**
     * Returns the data stream positioned to the first byte of the mail data. The caller must close
     * the returned stream.
     */
    default MaildataInputStream getInputStream() throws MaildataReadException {
        return getInputStream(new Range(0, length()));
    }

    long length() throws MaildataReadException;

    /**
     * Releases resources, like temporary files.
     */
    @Override
    void close();
}
