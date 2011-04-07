package mireka.address.parser;

import java.text.ParseException;

import mireka.address.parser.Ipv4Parser.Ipv4;
import mireka.address.parser.Ipv6Parser.Ipv6;
import mireka.address.parser.ast.AddressLiteralRemotePartAST;
import mireka.address.parser.ast.Ipv4RemotePartAST;
import mireka.address.parser.ast.Ipv6RemotePartAST;
import mireka.address.parser.base.CharParser;
import mireka.address.parser.base.CharScanner;

/**
 * AddressLiteralParser parses the remote part of an SMTP mailbox (after the
 * '@').
 */
public class AddressLiteralParser extends CharParser {

    public AddressLiteralParser(CharScanner charScanner) {
        super(charScanner);
    }

    public AddressLiteralRemotePartAST parse() throws ParseException {
        AddressLiteralRemotePartAST remotePartAST =
                parseAddressLiteralRemotePart();
        scanner.pushBack(currentToken);
        decorateWithInetAddress(remotePartAST);
        return remotePartAST;
    }

    private AddressLiteralRemotePartAST parseAddressLiteralRemotePart()
            throws ParseException {
        pushPosition();
        pushSpelling();

        accept('[');

        scanner.pushBack(currentToken);
        AddressLiteralTagScanner addressLiteralTagScanner =
                new AddressLiteralTagScanner(scanner);
        AddressLiteralTagToken tagToken = addressLiteralTagScanner.scan();
        addressLiteralTagScanner.finish();
        currentToken = scanner.scan();
        spelling.append(tagToken.spelling);
        switch (tagToken.kind) {
        case DIGIT:
            Ipv4 ipv4AST = parseIpv4AddressLiteral();
            accept(']');
            return new Ipv4RemotePartAST(popPosition(), popSpelling(), ipv4AST);
        case IPv6:
            accept(':');
            Ipv6 ipv6AST = parseIpv6AddressLiteral();
            accept(']');
            return new Ipv6RemotePartAST(popPosition(), popSpelling(), ipv6AST);
        case STANDARDIZED_TAG:
            throw tagToken
                    .syntaxException("IPv4 address literal or 'IPv6' tag");
        default:
            throw new RuntimeException("Assertion failed");
        }
    }

    private Ipv4 parseIpv4AddressLiteral() throws ParseException {
        scanner.pushBack(currentToken);
        Ipv4 ipv4 = new Ipv4Parser(scanner).parseLeft();
        currentToken = scanner.scan();
        spelling.append(ipv4.spelling);
        return ipv4;
    }

    private Ipv6 parseIpv6AddressLiteral() throws ParseException {
        scanner.pushBack(currentToken);
        Ipv6 ipv6 = new Ipv6Parser(scanner).parseLeft();
        currentToken = scanner.scan();
        spelling.append(ipv6.spelling);
        return ipv6;
    }

    private void decorateWithInetAddress(AddressLiteralRemotePartAST remotePartAST) {
        if (remotePartAST instanceof Ipv4RemotePartAST) {
            Ipv4RemotePartAST ipv4RemotePartAST = (Ipv4RemotePartAST) remotePartAST;
            remotePartAST.addressBytes = ipv4RemotePartAST.ipv4.addressBytes;
            remotePartAST.address = ipv4RemotePartAST.ipv4.address;
        } else if (remotePartAST instanceof Ipv6RemotePartAST) {
            Ipv6RemotePartAST ipv6RemotePartAST = (Ipv6RemotePartAST) remotePartAST;
            remotePartAST.addressBytes = ipv6RemotePartAST.ipv6.addressBytes;
            remotePartAST.address = ipv6RemotePartAST.ipv6.address;
        } else {
            throw new RuntimeException("Assertion failed");
        }
    }

}
