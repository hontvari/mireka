package mireka.address.parser.base;

import java.text.ParseException;

/**
 * Abstract syntax tree node.
 */
public abstract class AST {
    /**
     * Position of the first character of the node in the input text. This
     * information makes error messages more useful, otherwise it has no role.
     */
    int position;

    public AST(int position) {
        this.position = position;
    }

    public ParseException syntaxException(String sentence) {
        return new ParseException("Syntax error. " + sentence
                + " Character position: " + position + ".", position);
    }

}
