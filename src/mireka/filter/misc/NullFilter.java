package mireka.filter.misc;

import mireka.filter.RecipientContext;
import mireka.filter.StatelessFilterType;
import mireka.smtp.RejectExceptionExt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NullFilter drops any recipient whose destination is {@link NullDestination}.
 */
public class NullFilter extends StatelessFilterType {
    private final Logger logger = LoggerFactory.getLogger(NullFilter.class);

    @Override
    public void recipient(RecipientContext recipientContext)
            throws RejectExceptionExt {
        if (!(recipientContext.getDestination() instanceof NullDestination))
            return;
        recipientContext.isResponsibilityTransferred = true;
        logger.debug("NullDestination, dropping the recipient.");
    }

}
