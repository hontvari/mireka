package mireka.submission;

import mireka.filter.MailTransaction;

public interface MailTransactionSpecification {
    boolean isSatisfiedBy(MailTransaction mailTransaction);

}
