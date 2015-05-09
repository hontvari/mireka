package mireka.smtp.address.parser.ast;

public class DomainPostmasterRecipientAST extends RecipientAST {
    /**
     * The mailbox where the localPart is Postmaster (case insensitive) and the
     * remotePart is always a {@link DomainRemotePartAST}.
     */
    public MailboxAST mailboxAST;

    public DomainPostmasterRecipientAST(int position, MailboxAST mailboxAST) {
        super(position);
        this.mailboxAST = mailboxAST;
    }
}