package mireka;

import org.subethamail.smtp.RejectException;

public class RejectExceptionExt extends RejectException {
    private static final long serialVersionUID = 1530775593602824560L;
    
    private final SmtpReply reply;

    public RejectExceptionExt(SmtpReply smtpReply) {
        super(smtpReply.code, smtpReply.text);
        smtpReply.checkFullySpecified();
        this.reply = smtpReply;
    }

    public SmtpReply getReply() {
        return reply;
    }
}
