package mireka.address.parser;

import java.text.ParseException;

import mireka.address.parser.ast.NullReversePathAST;
import mireka.address.parser.ast.PathAST;
import mireka.address.parser.ast.RealReversePathAST;
import mireka.address.parser.ast.ReversePathAST;
import mireka.address.parser.base.CharParser;

public class ReversePathParser extends CharParser {

    public ReversePathParser(String source) {
        super(source);
    }

    public ReversePathAST parse() throws ParseException {
        ReversePathAST reversePathAST = parseReversePath();
        if (currentToken.ch != -1)
            throw currentToken.otherSyntaxException("Superfluous characters "
                    + "after reverse path: {0}");
        return reversePathAST;
    }

    private ReversePathAST parseReversePath() throws ParseException {
        pushPosition();
        if (inputEquals("<>")) {
            accept('<');
            accept('>');
            return new NullReversePathAST(popPosition());
        } else {
            PathAST pathAST = parsePath();
            return new RealReversePathAST(popPosition(), pathAST);
        }
    }

    private boolean inputEquals(String s) {
        return peekString(s.length()).equals(s);
    }

    private PathAST parsePath() throws ParseException {
        scanner.pushBack(currentToken);
        PathAST pathAST = new PathParser(scanner).parseLeft();
        currentToken = scanner.scan();
        return pathAST;
    }

}
