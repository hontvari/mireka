package mireka.smtp.address.parser.ast;

import mireka.smtp.address.parser.base.AST;

public class LocalPartAST extends AST {
    public String spelling;

    // public String displayable;

    public LocalPartAST(int position, String spelling) {
        super(position);
        this.spelling = spelling;
    }

}