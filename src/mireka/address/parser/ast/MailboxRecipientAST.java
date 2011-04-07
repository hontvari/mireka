package mireka.address.parser.ast;


public class MailboxRecipientAST extends RecipientAST {
    public PathAST pathAST;

    public MailboxRecipientAST(int position, PathAST pathAST) {
        super(position);
        this.pathAST = pathAST;
    }
}