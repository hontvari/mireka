package mireka.filter.builtin.spf;

import java.io.IOException;
import java.io.InputStream;

import mireka.filter.AbstractFilter;
import mireka.filter.Filter;
import mireka.filter.FilterType;
import mireka.filter.MailData;
import mireka.filter.MailTransaction;

import org.apache.james.jspf.executor.SPFResult;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.util.TextUtils;

public class AddReceivedSpfHeader implements FilterType {

    @Override
    public Filter createInstance(MailTransaction mailTransaction) {
        return new FilterImpl(mailTransaction, new SpfChecker(mailTransaction));
    }

    private class FilterImpl extends AbstractFilter {
        private final SpfChecker spfChecker;

        public FilterImpl(MailTransaction mailTransaction, SpfChecker spfChecker) {
            super(mailTransaction);
            this.spfChecker = spfChecker;
        }

        @Override
        public void data(MailData data) throws RejectException,
                TooMuchDataException, IOException {
            SPFResult spfResult = spfChecker.getResult();
            String headerString = spfResult.getHeader() + "\r\n";
            byte[] headerOctets = TextUtils.getAsciiBytes(headerString);
            SpfHeaderPrependedMailData prependedMailData =
                    new SpfHeaderPrependedMailData(headerOctets, data);

            chain.data(prependedMailData);
        }
    }

    private class SpfHeaderPrependedMailData implements MailData {
        private final MailData originalMailData;
        private final byte[] headerOctets;

        public SpfHeaderPrependedMailData(byte[] headerOctets,
                MailData originalMailData) {
            this.headerOctets = headerOctets;
            this.originalMailData = originalMailData;
        }

        @Override
        public InputStream getInputStream() {
            InputStream originalMailDataInputStream =
                    originalMailData.getInputStream();
            return new PrependingInputStream(headerOctets,
                    originalMailDataInputStream);
        }

    }

}
