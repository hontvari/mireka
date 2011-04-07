package mireka.address.parser;

import static mireka.address.parser.CharClasses.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import mireka.address.parser.AddressLiteralTagToken.Kind;
import mireka.address.parser.base.CharClass;
import mireka.address.parser.base.CharScanner;
import mireka.address.parser.base.CharToken;

public class AddressLiteralTagScanner {
    private CharScanner charScanner;

    private CharToken currentCharToken;
    /**
     * The same as {@link #currentCharToken}.ch;
     */
    private int currentChar;
    private List<CharToken> currentSpelling = new ArrayList<CharToken>();

    public AddressLiteralTagScanner(CharScanner charScanner) {
        this.charScanner = charScanner;
        currentCharToken = charScanner.scan();
        currentChar = currentCharToken.ch;
    }

    public AddressLiteralTagToken scan() throws ParseException {
        int position = currentCharToken.position;
        currentSpelling.clear();
        Kind kind = scanToken();
        return new AddressLiteralTagToken(position, currentSpelling, kind);
    }

    private AddressLiteralTagToken.Kind scanToken() throws ParseException {
        // the first character can be a digit according to RFC 5321, but this
        // would make difficult to decide if the source is a tag or the first
        // digit of an IPv4 address literal. So assume that it starts with
        // a letter. As of 2011-03 the only valid tag is IPv6, so this
        // assumption is valid.
        if (ALPHA.isSatisfiedBy(currentChar)) {
            scanStandardizedTag();
            return Kind.STANDARDIZED_TAG;
        } else if (CharClasses.DIGIT.isSatisfiedBy(currentChar)) {
            return Kind.DIGIT;
        } else {
            throw currentCharToken.syntaxException("The first digit of an "
                    + "IPv4 address, or an address type tag, like 'IPv6'");
        }
    }

    private void scanStandardizedTag() throws ParseException {
        while (LDH.isSatisfiedBy(currentChar)) {
            if (LET_DIG.isSatisfiedBy(currentChar)) {
                takeIt();
            } else if (currentChar == '-') {
                takeIt();
                while (currentChar == '-')
                    takeIt();
                take(LET_DIG);
            } else {
                throw currentCharToken
                        .syntaxException("letter, digit or hyphen");
            }
        }
    }

    private void take(CharClass charClass) throws ParseException {
        if (charClass.isSatisfiedBy(currentChar))
            takeIt();
        else
            throw currentCharToken.syntaxException(charClass);
    }

    private void takeIt() {
        currentSpelling.add(currentCharToken);
        currentCharToken = charScanner.scan();
        currentChar = currentCharToken.ch;
    }

    /**
     * Unreads lookahead data from the underlying character scanner.
     */
    public void finish() {
        charScanner.pushBack(currentCharToken);
    }
}
