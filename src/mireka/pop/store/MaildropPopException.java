package mireka.pop.store;

import mireka.pop.Pop3Exception;

/**
 * Thrown if an error occurs in the mail retrieval part, related to the POP3
 * protocol, of the maildrop (in contrast to the mail insertion part which is
 * related to the SMTP protocol).
 */
public class MaildropPopException extends Pop3Exception {
    private static final long serialVersionUID = 4327972231889535756L;

    public MaildropPopException(String responseCode, String message) {
        super(responseCode, message);
    }
}
