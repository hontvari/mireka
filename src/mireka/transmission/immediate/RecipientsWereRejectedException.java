package mireka.transmission.immediate;

import java.util.ArrayList;
import java.util.List;

/**
 * it is thrown if some - maybe all - recipients were rejected
 */
public class RecipientsWereRejectedException extends Exception {
    private static final long serialVersionUID = 3277656155722747405L;
    public final List<RecipientRejection> rejections;

    public RecipientsWereRejectedException(List<RecipientRejection> rejections) {
        this.rejections = new ArrayList<RecipientRejection>(rejections);
    }
}
