package mireka.address.parser.base;

import java.util.Collections;
import java.util.List;

public class CharToken extends Token {
    public int ch;

    public CharToken(int ch, int position) {
        super(position);
        this.ch = ch;
        this.position = position;
    }

    @Override
    public List<CharToken> getSpellingTokens() {
        return Collections.singletonList(this);
    }

    @Override
    public String toString() {
        return CharUtil.toVisibleChar(ch);
    }
}
