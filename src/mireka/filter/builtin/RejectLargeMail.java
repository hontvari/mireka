package mireka.filter.builtin;

import java.io.IOException;
import java.io.InputStream;

import mireka.filter.AbstractFilter;
import mireka.filter.Filter;
import mireka.filter.FilterType;
import mireka.filter.MailData;
import mireka.filter.MailTransaction;

import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

public class RejectLargeMail implements FilterType {
    private int maxAllowedSize = 20000000;

    @Override
    public Filter createInstance(MailTransaction mailTransaction) {
        return new FilterImpl(mailTransaction);
    }

    /**
     * @category GETSET
     */
    public int getMaxAllowedSize() {
        return maxAllowedSize;
    }

    /**
     * @category GETSET
     */
    public void setMaxAllowedSize(int maxAllowedSize) {
        this.maxAllowedSize = maxAllowedSize;
    }

    private class FilterImpl extends AbstractFilter {

        public FilterImpl(MailTransaction mailTransaction) {
            super(mailTransaction);
        }

        @Override
        public void data(MailData data) throws RejectException,
                TooMuchDataException, IOException {
            chain.data(new LengthLimitingMailData(data));
        }
    }

    private final class LengthLimitingMailData implements MailData {
        private final MailData mailData;

        public LengthLimitingMailData(MailData mailData) {
            this.mailData = mailData;
        }

        @Override
        public InputStream getInputStream() {
            return new LengthLimitingInputStream(mailData.getInputStream(),
                    maxAllowedSize);
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
