package mireka.filter.misc;

import static mireka.maildata.FieldDef.*;

import java.io.IOException;

import mireka.filter.StatelessFilterType;
import mireka.maildata.Maildata;

import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

public class StopLoop extends StatelessFilterType {
    private int maxReceivedHeaders = 100;

    @Override
    public void data(Maildata data) throws RejectException,
            TooMuchDataException, IOException {
        if (data.headers().countOf(RECEIVED) > maxReceivedHeaders)
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
