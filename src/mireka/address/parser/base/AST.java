package mireka.address.parser.base;

import java.text.ParseException;

/**
 * Abstract syntax tree node.
 */
public abstract class AST {
    int position;

    public AST(int position) {
        this.position = position;
    }

    public ParseException syntaxException(String sentence) {
        return new ParseException("Syntax error. " + sentence
                + " Character position: " + position + ".", position);
    }

}
