package mireka.smtp.address.parser;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import mireka.smtp.address.parser.ast.DomainAST;
import mireka.smtp.address.parser.ast.MailboxAST;
import mireka.smtp.address.parser.ast.PathAST;
import mireka.smtp.address.parser.ast.SourceRouteAST;
import mireka.smtp.address.parser.base.CharParser;
import mireka.smtp.address.parser.base.CharScanner;

public class PathParser extends CharParser {

    public PathParser(String source) {
        this(new CharScanner(source));
    }

    public PathParser(CharScanner charScanner) {
        super(charScanner);
    }

    public PathAST parse() throws ParseException {
        PathAST pathAST = parsePath();
        if (currentToken.ch != -1)
            throw currentToken.otherSyntaxException("Superfluous characters "
                    + "after path: {0}.");
        return pathAST;
    }

    public PathAST parseLeft() throws ParseException {
        PathAST pathAST = parsePath();
        scanner.pushBack(currentToken);
        return pathAST;
    }

    private PathAST parsePath() throws ParseException {
        pushPosition();
        SourceRouteAST sourceRouteAST = null;

        accept('<');
        if (currentToken.ch == '@') {
            sourceRouteAST = parseSourceRoute();
            accept(':');
        }
        MailboxAST mailboxAST = parseMailbox();
        accept('>');
        return new PathAST(popPosition(), sourceRouteAST, mailboxAST);
    }

    private SourceRouteAST parseSourceRoute() throws ParseException {
        pushPosition();
        List<DomainAST> domainASTs = new ArrayList<DomainAST>();
        do {
            accept('@');
            DomainAST domainAST = parseDomain();
            domainASTs.add(domainAST);
        } while (currentToken.ch == '@');
        return new SourceRouteAST(popPosition(), domainASTs);
    }

    private DomainAST parseDomain() throws ParseException {
        scanner.pushBack(currentToken);
        DomainAST domainAST = new DomainParser(scanner).parseLeft();
        spelling.append(domainAST.spelling);
        currentToken = scanner.scan();
        return domainAST;
    }

    private MailboxAST parseMailbox() throws ParseException {
        scanner.pushBack(currentToken);
        MailboxAST mailboxAST = new MailboxParser(scanner).parseLeft();
        currentToken = scanner.scan();
        return mailboxAST;
    }
}
