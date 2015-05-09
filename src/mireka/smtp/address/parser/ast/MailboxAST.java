package mireka.smtp.address.parser.ast;

import mireka.smtp.address.parser.base.AST;

public class MailboxAST extends AST {
    public String spelling;
    public LocalPartAST localPartAST;
    public RemotePartAST remotePartAST;

    public MailboxAST(int position, String spelling, LocalPartAST localPartAST,
            RemotePartAST remotePartAST) {
        super(position);
        this.spelling = spelling;
        this.localPartAST = localPartAST;
        this.remotePartAST = remotePartAST;
    }
}