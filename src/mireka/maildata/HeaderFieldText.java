package mireka.maildata;

/**
 * HeaderFieldText stores both the original spelling of a header field as it was
 * extracted from a mail and the same text in unfolded state.
 */
public class HeaderFieldText {
    public String originalSpelling;

    /**
     * The same as the originalSpelling except that any CRLF which is followed
     * by WSP is removed, and closing CRLF is removed as well.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc5322#section-2.2.3">RFC5322
     *      Long Header Fields</a>
     */
    public String unfoldedSpelling;
}
