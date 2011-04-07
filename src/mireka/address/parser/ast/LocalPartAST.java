package mireka.address.parser.ast;

import mireka.address.parser.base.AST;

public class LocalPartAST extends AST {
    public String spelling;

    // public String displayable;

    public LocalPartAST(int position, String spelling) {
        super(position);
        this.spelling = spelling;
    }

}