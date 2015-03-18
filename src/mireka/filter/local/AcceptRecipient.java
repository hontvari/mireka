package mireka.filter.local;

import mireka.filter.FilterReply;
import mireka.filter.RecipientContext;
import mireka.filter.StatelessFilterType;
import mireka.filter.local.table.RecipientSpecification;

import org.subethamail.smtp.RejectException;

public class AcceptRecipient extends StatelessFilterType {
    private RecipientSpecification recipientSpecification;

    @Override
    public FilterReply verifyRecipient(RecipientContext recipientContext)
            throws RejectException {
        if (recipientSpecification.isSatisfiedBy(recipientContext.recipient))
            return FilterReply.ACCEPT;
        else
            return FilterReply.NEUTRAL;
    }

    /**
     * @x.category GETSET
     */
    public RecipientSpecification getRecipientSpecification() {
        return recipientSpecification;
    }

    /**
     * @x.category GETSET
     */
    public void setRecipientSpecification(
            RecipientSpecification recipientSpecification) {
        this.recipientSpecification = recipientSpecification;
    }

}
