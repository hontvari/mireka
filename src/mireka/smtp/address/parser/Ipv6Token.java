package mireka.smtp.address.parser;

import java.util.List;

import mireka.smtp.address.parser.base.CharToken;
import mireka.smtp.address.parser.base.StringToken;

public class Ipv6Token extends StringToken {
    public Kind kind;

    public Ipv6Token(int position, List<CharToken> spellingTokens, Kind kind) {
        super(position, spellingTokens);
        this.kind = kind;
    }

    public enum Kind {
        NUM, DOT, COLON, DOUBLE_COLON, OTHER, EOF;
    }
}
