package mireka.address.parser;

import java.util.List;

import mireka.address.parser.base.CharToken;
import mireka.address.parser.base.StringToken;

public class AddressLiteralTagToken extends StringToken {

    public Kind kind;

    public AddressLiteralTagToken(int position, List<CharToken> spellingTokens,
            Kind kind) {
        super(position, spellingTokens);
        this.kind = kind;
        if (kind == Kind.STANDARDIZED_TAG && spelling.equals("IPv6"))
            this.kind = Kind.IPv6;
    }

    public enum Kind {
        DIGIT, IPv6, STANDARDIZED_TAG;
    }
}
