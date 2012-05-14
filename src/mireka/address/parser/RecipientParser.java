package mireka.address.parser;

import java.text.ParseException;
import java.util.Locale;

import mireka.address.parser.ast.DomainPostmasterRecipientAST;
import mireka.address.parser.ast.DomainRemotePartAST;
import mireka.address.parser.ast.MailboxRecipientAST;
import mireka.address.parser.ast.PathAST;
import mireka.address.parser.ast.RecipientAST;
import mireka.address.parser.ast.SystemPostmasterRecipientAST;
import mireka.address.parser.base.CharParser;
import mireka.address.parser.base.CharScanner;

public class RecipientParser extends CharParser {

    public RecipientParser(CharScanner charScanner) {
        super(charScanner);
    }

    public RecipientParser(String source) {
        super(source);
    }

    public RecipientAST parse() throws ParseException {
        RecipientAST recipientAST = parseRecipient();
        if (currentToken.ch != -1)
            throw currentToken.otherSyntaxException("Superfluous characters "
                    + "after recipient: {0}");
        return recipientAST;
    }

    private RecipientAST parseRecipient() throws ParseException {
        String DOMAIN_POSTMASTER_PREFIX = "<Postmaster@";
        String SYSTEM_POSTMASTER_PREFIX = "<Postmaster>";
        pushPosition();
        if (inputEqualsIgnoreCase(DOMAIN_POSTMASTER_PREFIX)) {
            PathAST pathAST = parsePath();
            if (pathAST.mailboxAST.remotePartAST instanceof DomainRemotePartAST)
                return new DomainPostmasterRecipientAST(popPosition(),
                        pathAST.mailboxAST);
            else
                return new MailboxRecipientAST(popPosition(), pathAST);
        } else if (inputEqualsIgnoreCase(SYSTEM_POSTMASTER_PREFIX)) {
            accept('<');
            pushSpelling();
            acceptThem(SYSTEM_POSTMASTER_PREFIX.length() - 2);
            String postmasterSpelling = popSpelling();
            accept('>');
            return new SystemPostmasterRecipientAST(popPosition(),
                    postmasterSpelling);
        } else {
            PathAST pathAST = parsePath();
            return new MailboxRecipientAST(popPosition(), pathAST);
        }
    }

    private boolean inputEqualsIgnoreCase(String s) {
        String sLowerCase = s.toLowerCase(Locale.US);
        return peekString(sLowerCase.length()).toLowerCase(Locale.US).equals(
                sLowerCase);
    }

    private PathAST parsePath() throws ParseException {
        scanner.pushBack(currentToken);
        PathAST pathAST = new PathParser(scanner).parseLeft();
        currentToken = scanner.scan();
        return pathAST;
    }
}
