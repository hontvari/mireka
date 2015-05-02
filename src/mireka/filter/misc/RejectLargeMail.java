package mireka.filter.misc;

import java.io.IOException;
import java.io.InputStream;

import mireka.filter.AbstractFilter;
import mireka.filter.Filter;
import mireka.filter.FilterType;
import mireka.filter.MailTransaction;
import mireka.smtp.RejectExceptionExt;

import org.subethamail.smtp.TooMuchDataException;

public class RejectLargeMail implements FilterType {
    private int maxAllowedSize = 25000000;

    @Override
    public Filter createInstance(MailTransaction mailTransaction) {
        return new FilterImpl(mailTransaction);
    }

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

    private class FilterImpl extends AbstractFilter {

        public FilterImpl(MailTransaction mailTransaction) {
            super(mailTransaction);
        }

        @Override
        public void dataStream(InputStream in) throws RejectExceptionExt,
                TooMuchDataException, IOException {
            chain.dataStream(new LengthLimitingInputStream(in, maxAllowedSize));
        }

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
