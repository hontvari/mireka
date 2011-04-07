package mireka.address.parser.ast;

import mireka.address.parser.base.AST;

public abstract class RemotePartAST extends AST {
    public String spelling;

    public RemotePartAST(int position, String spelling) {
        super(position);
        this.spelling = spelling;
    }
}