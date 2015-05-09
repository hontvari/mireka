package mireka.filter.misc;

import static mireka.maildata.FieldDef.*;
import mireka.filter.MailTransaction;
import mireka.filter.StatelessFilter;

import org.subethamail.smtp.RejectException;

/**
 * The StopLoop filter rejects a mail if it contains more than the configured
 * number of Received headers, because a large number of relays indicates that
 * the mail is sent around in a loop.
 */
public class StopLoop extends StatelessFilter {
    private int maxReceivedHeaders = 100;

    @Override
    public void data(MailTransaction transaction) throws RejectException {
        if (transaction.data.headers().countOf(RECEIVED) > maxReceivedHeaders)
            throw new RejectException(554, "Routing loop detected");
    }

    /**
     * @x.category GETSET
     */
    public int getMaxReceivedHeaders() {
        return maxReceivedHeaders;
    }

    /**
     * @x.category GETSET
     */
    public void setMaxReceivedHeaders(int maxReceivedHeaders) {
        this.maxReceivedHeaders = maxReceivedHeaders;
    }

}
