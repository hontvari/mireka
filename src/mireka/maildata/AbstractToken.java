package mireka.maildata;

import java.text.ParseException;

import mireka.maildata.FieldParser.TokenKind;

public abstract class AbstractToken {
    public String spelling;
    public int position;

    public ParseException syntaxException(TokenKind expected) {
        return new ParseException("Syntax error. Expected: "
                + expected.toString() + ", received: " + toString()
                + " at character position " + position + ".",
                (int) position);
    }

    public ParseException syntaxException(String expected) {
        return new ParseException("Syntax error. Expected: "
                + expected + ", received: " + toString()
                + " at character position " + position + ".",
                (int) position);
    }

    public ParseException unexpectedHereSyntaxException(String where) {
        return new ParseException("Syntax error. Unexpected token: '"
                + toString() + "', at the position: '" + where
                + "' at character position " + position + ".",
                (int) position);
    }

    @Override
    public String toString() {
        if (spelling == null || spelling.isEmpty()) {
            return getKindAsString();
        } else {
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < spelling.length(); i++)
                buffer.append(toVisibleChar(spelling.charAt(i)));
            return "'" + buffer.toString() + "'";
        }
    }
    
    protected abstract String getKindAsString();

    private static String toVisibleChar(char ch) {
        if (ch == 127 || ch < 32)
            return toUnicodeEscape(ch);
        else
            return "'" + (char) ch + "'";
    }

    private static String toUnicodeEscape(int ch) {
        return "\\u" + String.format("%04X", ch);
    }
    
}
