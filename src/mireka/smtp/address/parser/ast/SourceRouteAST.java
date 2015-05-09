package mireka.smtp.address.parser.ast;

import java.util.List;

import mireka.smtp.address.parser.base.AST;

public class SourceRouteAST extends AST {
    public List<DomainAST> domainASTs;

    public SourceRouteAST(int position, List<DomainAST> domainASTs) {
        super(position);
        this.domainASTs = domainASTs;
    }

}