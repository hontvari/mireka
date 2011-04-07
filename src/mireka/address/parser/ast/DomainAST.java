package mireka.address.parser.ast;

import mireka.address.parser.base.AST;

public class DomainAST extends AST {
    public String spelling;

    public DomainAST(int position, String spelling) {
        super(position);
        this.spelling = spelling;
    }

}