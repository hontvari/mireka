package mireka.filter.misc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import mireka.MailData;
import mireka.filter.AbstractFilter;
import mireka.filter.Filter;
import mireka.filter.FilterType;
import mireka.filter.MailTransaction;
import mireka.smtp.RejectExceptionExt;
import mireka.util.StreamCopier;

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
        public void data(MailData data) throws RejectExceptionExt,
                TooMuchDataException, IOException {
            chain.data(new LengthLimitingMailData(data));
        }
    }

    private final class LengthLimitingMailData implements MailData {
        private final MailData wrappedMailData;

        public LengthLimitingMailData(MailData sourceMailData) {
            this.wrappedMailData = sourceMailData;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new LengthLimitingInputStream(
                    wrappedMailData.getInputStream(), maxAllowedSize);
        }

        @Override
        public void writeTo(OutputStream out) throws IOException {
            StreamCopier.writeMailDataInputStreamIntoOutputStream(this, out);
        }

        @Override
        public void dispose() {
            wrappedMailData.dispose();
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
