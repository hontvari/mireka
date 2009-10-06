package mireka.filter.builtin.spf;

import org.apache.james.jspf.executor.SPFResult;
import org.apache.james.jspf.impl.DefaultSPF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mireka.filter.MailTransaction;

public class SpfChecker {
    private final static String SPF_RESULT_NAME = "mireka.spfResult";
    private final Logger logger = LoggerFactory.getLogger(SpfChecker.class);
    private final MailTransaction mailTransaction;

    public SpfChecker(MailTransaction mailTransaction) {
        this.mailTransaction = mailTransaction;
    }

    public SPFResult getResult() {
        SPFResult result =
                (SPFResult) mailTransaction.getAttribute(SPF_RESULT_NAME);
        if (result == null) {
            result = check();
            mailTransaction.setAttribute(SPF_RESULT_NAME, result);
        }
        return result;
    }

    private SPFResult check() {
        DefaultSPF spf = new DefaultSPF(new Slf4jToJspfLoggerAdapter());
        spf.setUseBestGuess(false);
        // null reverse path should correspond to empty string
        String fromNonNull =
                mailTransaction.getFrom() == null ? "" : mailTransaction
                        .getFrom();
        String helo = mailTransaction.getMessageContext().getHelo();
        String heloNonNull =
                helo == null ? "["
                        + mailTransaction.getRemoteInetAddress()
                                .getHostAddress() + "]" : helo;
        SPFResult result =
                spf.checkSPF(mailTransaction.getRemoteInetAddress()
                        .getHostAddress(), fromNonNull, heloNonNull);
        logger.debug("SPF check result: {} {}", result.getResult(), result
                .getExplanation());
        return result;
    }
}
