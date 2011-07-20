package mireka.address.parser.ast;

/**
 * RealReversePathAST represents a non-null reverse path.
 */
public class RealReversePathAST extends ReversePathAST {
    public PathAST pathAST;

    public RealReversePathAST(int position, PathAST pathAST) {
        super(position);
        this.pathAST = pathAST;
    }
}
