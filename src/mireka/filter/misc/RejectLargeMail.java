package mireka.filter.misc;

import java.io.IOException;
import java.io.InputStream;

import mireka.filter.MailTransaction;
import mireka.filter.StatelessFilter;

import org.subethamail.smtp.TooMuchDataException;

/**
 * This filter rejects the mail data if it is larger than the configured limit.
 * It installs a wrapper around the mail data stream, which throws an exception
 * if the stream would return more bytes than the configured limit.
 */
public class RejectLargeMail extends StatelessFilter {
    private int maxAllowedSize = 25000000;

    /**
     * @x.category GETSET
     */
    public int getMaxAllowedSize() {
        return maxAllowedSize;
    }

    /**
     * @x.category GETSET
     */
    public void setMaxAllowedSize(int maxAllowedSize) {
        this.maxAllowedSize = maxAllowedSize;
    }

    @Override
    public void dataStream(MailTransaction transaction) {
        transaction.dataStream =
                new LengthLimitingInputStream(transaction.dataStream,
                        maxAllowedSize);
    }

    private final class LengthLimitingInputStream extends
            ThresholdingInputStream {
        private LengthLimitingInputStream(InputStream in, int thresholdBytes) {
            super(in, thresholdBytes);
        }

        @Override
        protected void thresholdReached(int current) throws IOException {
            throw new TooMuchDataException();
        }
    }

}
