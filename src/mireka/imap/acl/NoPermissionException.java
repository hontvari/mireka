package mireka.imap.acl;

import mireka.imap.CompletionException;
import mireka.imap.ResponseCode;

public class NoPermissionException extends CompletionException {
    private static final long serialVersionUID = -3035780607501481705L;

    public NoPermissionException() {
        super(ResponseCode.NOPERM, "Access denied");
    }
}
