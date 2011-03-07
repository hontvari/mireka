package mireka.list;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mireka.MailData;
import mireka.filter.AbstractDataRecipientFilter;
import mireka.filter.DataRecipientFilterAdapter;
import mireka.filter.Filter;
import mireka.filter.FilterType;
import mireka.filter.MailTransaction;
import mireka.filter.RecipientContext;
import mireka.smtp.RejectExceptionExt;
import mireka.transmission.Mail;

import org.subethamail.smtp.TooMuchDataException;

/**
 * This filter class redistribute the incoming mail to a mailing list.
 */
public class MailingListFilter implements FilterType {

    @Override
    public Filter createInstance(MailTransaction mailTransaction) {
        FilterImpl filterInstance = new FilterImpl(mailTransaction);
        return new DataRecipientFilterAdapter(filterInstance, mailTransaction);
    }

    private class FilterImpl extends AbstractDataRecipientFilter {
        private Mail mail = new Mail();
        private List<RecipientMailPair> recipientMailPairs =
                new ArrayList<MailingListFilter.RecipientMailPair>();

        protected FilterImpl(MailTransaction mailTransaction) {
            super(mailTransaction);
        }

        @Override
        public void from(String from) {
            mail.from = from;
            mail.receivedFromMtaAddress =
                    mailTransaction.getRemoteInetAddress();
            mail.receivedFromMtaName =
                    mailTransaction.getMessageContext().getHelo();
        }

        @Override
        public void recipient(RecipientContext recipientContext) {
            if (!(recipientContext.getDestination() instanceof ListDestination))
                return;
            recipientContext.isResponsibilityTransferred = true;
            RecipientMailPair recipientMailPair = new RecipientMailPair();
            recipientMailPair.recipientContext = recipientContext;
            recipientMailPair.mail = new Mail();
            recipientMailPair.mail.from = mail.from;
            recipientMailPair.mail.receivedFromMtaAddress =
                    mail.receivedFromMtaAddress;
            recipientMailPair.mail.receivedFromMtaName =
                    mail.receivedFromMtaName;
            recipientMailPair.mail.recipients.add(recipientContext.recipient);
            recipientMailPairs.add(recipientMailPair);
        }

        @Override
        public void data(MailData data) throws TooMuchDataException,
                RejectExceptionExt, IOException {
            for (RecipientMailPair recipientMailPair : recipientMailPairs) {
                recipientMailPair.mail.mailData = data;
                recipientMailPair.mail.arrivalDate = new Date();
                recipientMailPair.mail.scheduleDate = mail.arrivalDate;
                ListDestination destination =
                        (ListDestination) recipientMailPair.recipientContext
                                .getDestination();
                destination.getList().submit(recipientMailPair.mail);
            }
        }
    }

    private static class RecipientMailPair {
        RecipientContext recipientContext;
        Mail mail;
    }
}
