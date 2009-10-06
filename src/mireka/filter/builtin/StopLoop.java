package mireka.filter.builtin;

import java.io.IOException;

import mireka.filter.MailData;
import mireka.filter.StatelessFilterType;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.io.MaxLineLimitException;
import org.apache.james.mime4j.parser.MimeTokenStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

public class StopLoop extends StatelessFilterType {
    private final Logger logger = LoggerFactory.getLogger(StopLoop.class);
    private int maxReceivedHeaders = 100;

    @Override
    public void data(MailData data) throws RejectException,
            TooMuchDataException, IOException {
        try {
            if (receivedHeaderCount(data) > maxReceivedHeaders)
                throw new RejectException(554,
                        "Routing loop detected");
        } catch (MimeException e) {
            logger.debug("Cannot determine Received header count", e);
            throw new RejectException(554, "Invalid message content");
        } catch (MaxLineLimitException e) {
            logger.debug("Line too long, cannot determine Received header count", e);
            throw new RejectException(554, "Line too long");
        } 
    }

    private int receivedHeaderCount(MailData data) throws IOException,
            MimeException {
        int count = 0;
        MimeTokenStream stream = new MimeTokenStream();
        stream.parse(data.getInputStream());
        for (int state = stream.getState(); state != MimeTokenStream.T_END_OF_STREAM; state =
                stream.next()) {
            switch (state) {
            case MimeTokenStream.T_FIELD:
                if ("Received".equalsIgnoreCase(stream.getField().getName()))
                    count++;
                break;
            case MimeTokenStream.T_END_HEADER:
                stream.stop();
                break;
            }
        }
        return count;
    }

    /**
     * @category GETSET
     */
    public int getMaxReceivedHeaders() {
        return maxReceivedHeaders;
    }

    /**
     * @category GETSET
     */
    public void setMaxReceivedHeaders(int maxReceivedHeaders) {
        this.maxReceivedHeaders = maxReceivedHeaders;
    }

}
