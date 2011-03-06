package mireka.pop;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import mireka.MailData;
import mireka.filter.AbstractDataRecipientFilter;
import mireka.filter.DataRecipientFilterAdapter;
import mireka.filter.Filter;
import mireka.filter.FilterType;
import mireka.filter.MailTransaction;
import mireka.filter.RecipientContext;
import mireka.pop.store.Maildrop;
import mireka.pop.store.MaildropAppender;
import mireka.pop.store.MaildropRepository;
import mireka.smtp.EnhancedStatus;
import mireka.transmission.LocalMailSystemException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

/**
 * MaildropFilter puts the mail into a maildrop for those recipients whose
 * assigned destination is {@link MaildropDestination}.
 */
public class MaildropFilter implements FilterType {
    private MaildropRepository maildropRepository;

    @Override
    public Filter createInstance(MailTransaction mailTransaction) {
        FilterImpl filterInstance = new FilterImpl(mailTransaction);
        return new DataRecipientFilterAdapter(filterInstance, mailTransaction);
    }

    /**
     * GETSET
     */
    public void setMaildropRepository(MaildropRepository maildropRepository) {
        this.maildropRepository = maildropRepository;
    }

    /**
     * GETSET
     */
    public MaildropRepository getMaildropRepository() {
        return maildropRepository;
    }

    private class FilterImpl extends AbstractDataRecipientFilter {
        private final Logger logger = LoggerFactory.getLogger(FilterImpl.class);

        private List<String> maildropNames = new ArrayList<String>();

        protected FilterImpl(MailTransaction mailTransaction) {
            super(mailTransaction);
        }

        @Override
        public void from(String from) {
            // do nothing
        }

        @Override
        public void recipient(RecipientContext recipientContext)
                throws RejectException {
            if (!(recipientContext.getDestination() instanceof MaildropDestination))
                return;
            recipientContext.isResponsibilityTransferred = true;
            MaildropDestination maildropDestination =
                    (MaildropDestination) recipientContext.getDestination();
            maildropNames.add(maildropDestination.getMaildropName());
        }

        @Override
        public void data(MailData data) throws TooMuchDataException,
                RejectException, IOException {
            for (String maildropName : maildropNames) {
                Maildrop maildrop =
                        maildropRepository.borrowMaildrop(maildropName);
                try {

                    MaildropAppender appender;
                    try {
                        appender = maildrop.allocateAppender();
                    } catch (LocalMailSystemException e) {
                        logger.error("Cannot accept mail because of a "
                                + "maildrop failure", e);
                        throw e.errorStatus().createRejectException();
                    }
                    OutputStream out;
                    try {
                        out = appender.getOutputStream();
                    } catch (LocalMailSystemException e) {
                        logger.error("Cannot accept mail because of a "
                                + "maildrop failure", e);
                        appender.rollback();
                        throw e.errorStatus().createRejectException();
                    }
                    try {
                        data.writeTo(out);
                    } catch (IOException e) {
                        logger.error(
                                "Cannot accept mail because of an IO error "
                                        + "occured while the mail was written into the maildrop",
                                e);
                        appender.rollback();
                        throw EnhancedStatus.TRANSIENT_LOCAL_ERROR_IN_PROCESSING
                                .createRejectException();
                    }
                    try {
                        appender.commit();
                    } catch (LocalMailSystemException e) {
                        logger.error("Cannot accept mail because of a "
                                + "maildrop failure", e);
                        throw e.errorStatus().createRejectException();
                    }
                } finally {
                    maildropRepository.releaseMaildrop(maildrop);
                }
            }
        }
    }
}
