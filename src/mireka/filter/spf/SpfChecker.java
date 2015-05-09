package mireka.filter.spf;

import mireka.filter.MailTransaction;

import org.apache.james.jspf.executor.SPFResult;
import org.apache.james.jspf.impl.DefaultSPF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SpfChecker executes an SPF check and caches the result in the
 * {@link MailTransaction#spfResult} object.
 */
public class SpfChecker {
    private final Logger logger = LoggerFactory.getLogger(SpfChecker.class);
    private final MailTransaction mailTransaction;

    public SpfChecker(MailTransaction mailTransaction) {
        this.mailTransaction = mailTransaction;
    }

    /**
     * Returns the result of the SPF check, if it is already cached then returns
     * the result from the cache, otherwise it executes the SPF check, including
     * the necessary DNS queries.
     */
    public SPFResult getResult() {
        SPFResult result = mailTransaction.spfResult;
        if (result == null) {
            result = check();
            mailTransaction.spfResult = result;
        }
        return result;
    }

    private SPFResult check() {
        DefaultSPF spf = new DefaultSPF(new Slf4jToJspfLoggerAdapter());
        spf.setUseBestGuess(false);
        // null reverse path should correspond to empty string
        String fromNonNull = mailTransaction.reversePath.getSmtpText();
        String helo = mailTransaction.getMessageContext().getHelo();
        String heloNonNull =
                helo == null ? "["
                        + mailTransaction.getRemoteInetAddress()
                                .getHostAddress() + "]" : helo;
        SPFResult result =
                spf.checkSPF(mailTransaction.getRemoteInetAddress()
                        .getHostAddress(), fromNonNull, heloNonNull);
        logger.debug("SPF check result: {} {}", result.getResult(),
                result.getExplanation());
        return result;
    }
}
