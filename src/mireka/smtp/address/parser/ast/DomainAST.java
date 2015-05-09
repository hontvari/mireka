package mireka.smtp.address.parser.ast;

import mireka.smtp.address.parser.base.AST;

public class DomainAST extends AST {
    public String spelling;

    public DomainAST(int position, String spelling) {
        super(position);
        this.spelling = spelling;
    }

}