package mireka.filter.spf;

import mireka.filter.MailTransaction;
import mireka.filter.StatelessFilter;

import org.apache.james.jspf.executor.SPFResult;
import org.subethamail.smtp.util.TextUtils;

/**
 * This filter prepends the result of the SPF check to the mail data in a header
 * field.
 */
public class AddReceivedSpfHeader extends StatelessFilter {

    @Override
    protected void dataStream(MailTransaction transaction) {
        SPFResult spfResult = new SpfChecker(transaction).getResult();
        String headerString = spfResult.getHeader() + "\r\n";
        byte[] headerOctets = TextUtils.getAsciiBytes(headerString);
        transaction.dataStream =
                new PrependingInputStream(headerOctets, transaction.dataStream);
    }
}
