package mireka.filter.misc;

import java.io.IOException;

import mireka.filter.StatelessFilterType;
import mireka.maildata.MaildataFile;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.io.MaxLineLimitException;
import org.apache.james.mime4j.stream.EntityState;
import org.apache.james.mime4j.stream.MimeTokenStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

public class StopLoop extends StatelessFilterType {
    private final Logger logger = LoggerFactory.getLogger(StopLoop.class);
    private int maxReceivedHeaders = 100;

    @Override
    public void data(MaildataFile data) throws RejectException,
            TooMuchDataException, IOException {
        try {
            if (receivedHeaderCount(data) > maxReceivedHeaders)
                throw new RejectException(554, "Routing loop detected");
        } catch (MimeException e) {
            logger.debug("Cannot determine Received header count", e);
            throw new RejectException(554, "Invalid message content");
        } catch (MaxLineLimitException e) {
            logger.debug(
                    "Line too long, cannot determine Received header count", e);
            throw new RejectException(554, "Line too long");
        }
    }

    private int receivedHeaderCount(MaildataFile data) throws IOException,
            MimeException {
        int count = 0;
        MimeTokenStream stream = new MimeTokenStream();
        stream.parse(data.getInputStream());
        for (EntityState state = stream.getState(); state != EntityState.T_END_OF_STREAM; state =
                stream.next()) {
            switch (state) {
            case T_FIELD:
                if ("Received".equalsIgnoreCase(stream.getField().getName()))
                    count++;
                break;
            case T_END_HEADER:
                stream.stop();
                break;
            }
        }
        return count;
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
