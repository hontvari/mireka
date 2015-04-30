package mireka.filter.spf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import mireka.filter.AbstractFilter;
import mireka.filter.Filter;
import mireka.filter.FilterType;
import mireka.filter.MailTransaction;
import mireka.maildata.MaildataFile;
import mireka.smtp.RejectExceptionExt;
import mireka.util.StreamCopier;

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
        public void data(MaildataFile data) throws RejectExceptionExt,
                TooMuchDataException, IOException {
            SPFResult spfResult = spfChecker.getResult();
            String headerString = spfResult.getHeader() + "\r\n";
            byte[] headerOctets = TextUtils.getAsciiBytes(headerString);
            SpfHeaderPrependedMaildataFile prependedMailData =
                    new SpfHeaderPrependedMaildataFile(headerOctets, data);

            chain.data(prependedMailData);
        }
    }

    private class SpfHeaderPrependedMaildataFile implements MaildataFile {
        private final MaildataFile originalMailData;
        private final byte[] headerOctets;

        public SpfHeaderPrependedMaildataFile(byte[] headerOctets,
                MaildataFile originalMailData) {
            this.headerOctets = headerOctets;
            this.originalMailData = originalMailData;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            InputStream originalMailDataInputStream =
                    originalMailData.getInputStream();
            return new PrependingInputStream(headerOctets,
                    originalMailDataInputStream);
        }

        @Override
        public void writeTo(OutputStream out) throws IOException {
            StreamCopier.writeMailDataInputStreamIntoOutputStream(this, out);
        }

        @Override
        public void close() {
            originalMailData.close();
        }
    }
}
