package mireka.address.parser.base;

import java.util.ArrayList;
import java.util.List;

public abstract class StringToken extends Token {
    private List<CharToken> spellingTokens;
    public String spelling;

    protected StringToken(int position, List<CharToken> spellingTokens) {
        super(position);
        this.spellingTokens = new ArrayList<CharToken>(spellingTokens);
        this.spelling = createSpelling(spellingTokens);
    }

    private static String createSpelling(List<CharToken> spellingTokens) {
        StringBuilder buffer = new StringBuilder();
        for (CharToken token : spellingTokens) {
            if (token.ch != -1)
                buffer.append((char) token.ch);
        }
        return buffer.toString();
    }

    public List<CharToken> getChars() {
        return spellingTokens;
    }

    @Override
    public String toString() {
        return spelling;
    }

    @Override
    public List<CharToken> getSpellingTokens() {
        return spellingTokens;
    }

}
