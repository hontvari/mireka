package mireka.smtp.address.parser;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import mireka.smtp.address.parser.Ipv6Token.Kind;
import mireka.smtp.address.parser.base.CharClass;
import mireka.smtp.address.parser.base.CharScanner;
import mireka.smtp.address.parser.base.CharToken;

public class Ipv6Scanner {
    private CharScanner charScanner;

    private CharToken currentCharToken;
    private List<CharToken> currentSpelling = new ArrayList<CharToken>();

    public Ipv6Scanner(CharScanner charScanner) {
        this.charScanner = charScanner;
        currentCharToken = charScanner.scan();
    }

    public Ipv6Token scan() throws ParseException {
        int position = currentCharToken.position;
        currentSpelling.clear();
        Kind kind = scanToken();
        return new Ipv6Token(position, currentSpelling, kind);
    }

    private Kind scanToken() throws ParseException {
        if (CharClasses.HEX.isSatisfiedBy(currentCharToken.ch)) {
            scanHexDigits();
            return Kind.NUM;
        } else if (currentCharToken.ch == '.') {
            takeIt();
            return Kind.DOT;
        } else if (currentCharToken.ch == ':') {
            takeIt();
            if (currentCharToken.ch == ':') {
                takeIt();
                return Kind.DOUBLE_COLON;
            } else {
                return Kind.COLON;
            }
        } else if (currentCharToken.ch == -1) {
            takeIt();
            return Kind.EOF;
        } else {
            takeIt();
            return Kind.OTHER;
        }
    }

    private void scanHexDigits() throws ParseException {
        take(CharClasses.HEX);
        for (int i = 0; i < 3; i++) {
            if (CharClasses.HEX.isSatisfiedBy(currentCharToken.ch)) {
                takeIt();
            }
        }
    }

    private void take(CharClass charClass) throws ParseException {
        if (charClass.isSatisfiedBy(currentCharToken.ch))
            takeIt();
        else
            throw currentCharToken.syntaxException(charClass);
    }

    private void takeIt() {
        currentSpelling.add(currentCharToken);
        currentCharToken = charScanner.scan();
    }

    /**
     * Unreads lookahead data from the underlying character scanner.
     */
    public void finish(Ipv6Token lastUnparsedToken) {
        charScanner.pushBack(currentCharToken);
        charScanner.pushBack(lastUnparsedToken);
    }
}
