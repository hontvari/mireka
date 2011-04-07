package mireka.address.parser.base;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.List;

public abstract class Token {
    public int position;

    public Token(int position) {
        this.position = position;
    }

    public abstract List<CharToken> getSpellingTokens();

    public ParseException syntaxException(Object expected) {
        return new ParseException("Syntax error. Expected: " + expected
                + ", received: " + toString() + " at character position "
                + position + ".", position);
    }

    public ParseException otherSyntaxException(String sentence) {
        String formattedMessage = MessageFormat.format(sentence, this);
        return new ParseException("Syntax error. " + formattedMessage
                + " Character position: " + position + ".", position);
    }

    /**
     * Returns the token in a readable format which can be used in error
     * messages.
     */
    @Override
    public abstract String toString();

}
