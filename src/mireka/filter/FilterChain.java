package mireka.filter;

import java.io.IOException;

import mireka.MailData;
import mireka.address.ReversePath;
import mireka.smtp.RejectExceptionExt;

import org.subethamail.smtp.TooMuchDataException;

public interface FilterChain {
    void begin();

    void from(ReversePath from) throws RejectExceptionExt;

    FilterReply verifyRecipient(RecipientContext recipientContext)
            throws RejectExceptionExt;

    void recipient(RecipientContext recipientContext) throws RejectExceptionExt;

    /**
     * A typical implementation of this method would follow the following
     * pattern: 1. examine the complete mail data or only its headers 2.
     * optionally wrap the data object for example to prepend trace data 3.
     * invoke the next entity in the chain
     * <p>
     * The passed {@link MailData} object will become the return value of
     * {@link MailTransaction#getData()} until another filter replaces it
     * possibly by wrapping it
     */
    void data(MailData data) throws RejectExceptionExt, TooMuchDataException,
            IOException;
}
