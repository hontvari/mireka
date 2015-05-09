package mireka.smtp.address.parser.ast;

import mireka.smtp.address.parser.base.AST;

public class PathAST extends AST {
    public SourceRouteAST sourceRouteAST;
    public MailboxAST mailboxAST;

    public PathAST(int position, SourceRouteAST sourceRouteAST,
            MailboxAST mailboxAST) {
        super(position);
        this.sourceRouteAST = sourceRouteAST;
        this.mailboxAST = mailboxAST;
    }

}