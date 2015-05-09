package mireka.submission;

import mireka.filter.MailTransaction;

/**
 * A MailTransactionSpecification implementation checks if a MailTransaction
 * matches a condition.
 * 
 * This interface follows the specification pattern.
 */
public interface MailTransactionSpecification {
    boolean isSatisfiedBy(MailTransaction mailTransaction);

}
