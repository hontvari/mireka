package mireka.filter.spf;

import java.io.IOException;
import java.io.InputStream;

import mireka.filter.AbstractFilter;
import mireka.filter.Filter;
import mireka.filter.FilterType;
import mireka.filter.MailTransaction;
import mireka.smtp.RejectExceptionExt;

import org.apache.james.jspf.executor.SPFResult;
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
        public void dataStream(InputStream in) throws RejectExceptionExt,
                TooMuchDataException, IOException {
            SPFResult spfResult = spfChecker.getResult();
            String headerString = spfResult.getHeader() + "\r\n";
            byte[] headerOctets = TextUtils.getAsciiBytes(headerString);

            chain.dataStream(new PrependingInputStream(headerOctets, in));
        }

    }

}
