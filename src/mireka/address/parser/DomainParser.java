package mireka.address.parser;

import static mireka.address.parser.CharClasses.*;

import java.text.ParseException;

import mireka.address.parser.ast.DomainAST;
import mireka.address.parser.base.CharParser;
import mireka.address.parser.base.CharScanner;

public class DomainParser extends CharParser {

    protected DomainParser(CharScanner charScanner) {
        super(charScanner);
    }

    public DomainAST parseLeft() throws ParseException {
        DomainAST domainAST = parseDomain();
        scanner.pushBack(currentToken);
        return domainAST;
    }

    private DomainAST parseDomain() throws ParseException {
        pushPosition();
        pushSpelling();

        parseSubDomain();
        while (currentToken.ch == '.') {
            acceptIt();
            parseSubDomain();
        }
        return new DomainAST(popPosition(), popSpelling());
    }

    private void parseSubDomain() throws ParseException {
        parseLetDig();
        if (LDH.isSatisfiedBy(currentToken.ch))
            parseLdhStr();
    }

    private void parseLetDig() throws ParseException {
        if (LET_DIG.isSatisfiedBy(currentToken.ch))
            acceptIt();
        else
            throw currentToken.syntaxException("letter or digit");
    }

    /**
     * Original ABNF:
     * <ul>
     * <li>Let-dig = ALPHA / DIGIT
     * <li>Ldh-str = *( ALPHA / DIGIT / "-" ) Let-dig
     * </ul>
     * This corresponds to A*(A|B)A. This is not an LL(1) language, but it can
     * be transformed: A *(A|*BA)
     * 
     * Transformed:
     * <ul>
     * <li>Let-dig = ALPHA / DIGIT
     * <li>Ldh-str = *( Let-dig / (1*"-" Let-dig) )
     * </ul>
     * 
     * @throws ParseException
     * 
     */
    private void parseLdhStr() throws ParseException {
        while (LDH.isSatisfiedBy(currentToken.ch)) {
            if (LET_DIG.isSatisfiedBy(currentToken.ch)) {
                parseLetDig();
            } else if (currentToken.ch == '-') {
                acceptIt();
                while (currentToken.ch == '-')
                    acceptIt();
                parseLetDig();
            } else {
                throw currentToken.syntaxException("letter, digit or hyphen");
            }
        }
    }

}
