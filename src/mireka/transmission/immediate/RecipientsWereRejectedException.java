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
        super(rejectionsToString(rejections));
        this.rejections = new ArrayList<RecipientRejection>(rejections);
    }

    private static String rejectionsToString(List<RecipientRejection> rejections) {
        if (rejections.isEmpty())
            throw new IllegalArgumentException();
        if (rejections.size() == 1)
            return rejections.get(0).toString();
        else {
            return rejections.size() + "* " + rejections.get(0).toString()
                    + "...";
        }
    }
}
