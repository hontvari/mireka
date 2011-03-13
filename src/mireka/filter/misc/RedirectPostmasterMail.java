package mireka.filter.misc;

import java.util.ArrayList;
import java.util.List;

import mireka.filter.FilterReply;
import mireka.filter.RecipientContext;
import mireka.filter.StatelessFilterType;
import mireka.smtp.RejectExceptionExt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assigns a destination to a postmaster recipient depending on the reverse
 * path. This filter is useful if some public clients needs to send mail to a
 * specific address, but cannot store authentication information. In that case
 * it sends mail to the postmaster, which does not require authentication. This
 * filter sorts these mails bases on the reverse path. If no mapping matches the
 * reverse path then this filter does nothing.
 */
public class RedirectPostmasterMail extends StatelessFilterType {
    private final Logger logger = LoggerFactory
            .getLogger(RedirectPostmasterMail.class);
    private List<ReversePathDestinationPair> mappings =
            new ArrayList<ReversePathDestinationPair>();

    @Override
    public FilterReply verifyRecipient(RecipientContext recipientContext)
            throws RejectExceptionExt {
        assignDestination(recipientContext);
        return FilterReply.NEUTRAL;
    }

    private void assignDestination(RecipientContext recipientContext) {
        if (recipientContext.isDestinationAssigned())
            return;
        if (!recipientContext.recipient.isPostmaster())
            return;

        String reversePath = recipientContext.getMailTransaction().getFrom();
        for (ReversePathDestinationPair mapping : mappings) {
            if (mapping.getReversePath().equals(reversePath)) {
                recipientContext.setDestination(mapping.getDestination());
                logger.debug("Mail to " + recipientContext.recipient
                        + " was redirected to " + mapping.getDestination());
                return;
            }
        }
    }

    public void addMapping(ReversePathDestinationPair mapping) {
        mappings.add(mapping);
    }
}
