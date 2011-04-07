package mireka.address.parser;

import static mireka.address.parser.CharClasses.*;

import java.text.ParseException;

import mireka.address.parser.ast.AddressLiteralRemotePartAST;
import mireka.address.parser.ast.DomainAST;
import mireka.address.parser.ast.DomainRemotePartAST;
import mireka.address.parser.ast.RemotePartAST;
import mireka.address.parser.base.CharParser;
import mireka.address.parser.base.CharScanner;

public class RemotePartParser extends CharParser {

    public RemotePartParser(CharScanner charScanner) {
        super(charScanner);
    }

    public RemotePartParser(String source) {
        super(source);
    }

    public RemotePartAST parse() throws ParseException {
        RemotePartAST remotePartAST = parseRemotePart();
        if (currentToken.ch != -1)
            throw currentToken.otherSyntaxException("Superfluous characters "
                    + "after remote part: {0}");
        return remotePartAST;
    }

    public RemotePartAST parseLeft() throws ParseException {
        RemotePartAST remotePartAST = parseRemotePart();
        scanner.pushBack(currentToken);
        return remotePartAST;
    }

    private RemotePartAST parseRemotePart() throws ParseException {
        if (LET_DIG.isSatisfiedBy(currentToken.ch)) {
            pushPosition();
            pushSpelling();
            parseDomain();
            return new DomainRemotePartAST(popPosition(), popSpelling());
        } else if (currentToken.ch == '[') {
            AddressLiteralRemotePartAST addressLiteralRemotePartAST =
                    parseAddressLiteral();
            return addressLiteralRemotePartAST;
        } else {
            throw currentToken.syntaxException("domain or address literal");
        }
    }

    private void parseDomain() throws ParseException {
        scanner.pushBack(currentToken);
        DomainAST domainAST = new DomainParser(scanner).parseLeft();
        spelling.append(domainAST.spelling);
        currentToken = scanner.scan();
    }

    private AddressLiteralRemotePartAST parseAddressLiteral()
            throws ParseException {
        scanner.pushBack(currentToken);
        AddressLiteralRemotePartAST addressLiteralRemotePartAST =
                new AddressLiteralParser(scanner).parse();
        currentToken = scanner.scan();
        return addressLiteralRemotePartAST;
    }

}
