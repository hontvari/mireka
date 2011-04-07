package mireka.address.parser;

import static mireka.address.parser.CharClasses.*;

import java.text.ParseException;

import mireka.address.parser.ast.LocalPartAST;
import mireka.address.parser.ast.MailboxAST;
import mireka.address.parser.ast.RemotePartAST;
import mireka.address.parser.base.CharClass;
import mireka.address.parser.base.CharParser;
import mireka.address.parser.base.CharScanner;

/**
 * MailboxParser parses an SMTP address, see mailbox production in the RFC.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc5321#section-4.1.2">RFC 5321</a>
 */
public class MailboxParser extends CharParser {
    /**
     * Printable US-ASCII characters not including specials. Specials are
     * defined in RFC 5322 Internet Message Format.
     */
    private static final CharClass ATEXT = new CharClass() {

        @Override
        public boolean isSatisfiedBy(int ch) {
            return ALPHA.isSatisfiedBy(ch) || DIGIT.isSatisfiedBy(ch)
                    || ch == '!' || ch == '#' || ch == '$' || ch == '%'
                    || ch == '&' || ch == '\'' || ch == '*' || ch == '+'
                    || ch == '-' || ch == '/' || ch == '=' || ch == '?'
                    || ch == '^' || ch == '_' || ch == '`' || ch == '{'
                    || ch == '|' || ch == '}' || ch == '~';
        }

        @Override
        public String toString() {
            return "atext";
        };
    };

    /**
     * Within a quoted string, any ASCII graphic or space is permitted without
     * backslash-quoting except double-quote and the backslash itself.
     */
    private static final CharClass QTEXT_SMTP = new CharClass() {

        @Override
        public boolean isSatisfiedBy(int ch) {
            return (32 <= ch && ch <= 33) || (35 <= ch && ch <= 91)
                    || (93 <= ch && ch <= 126);
        }

        @Override
        public String toString() {
            return "qtestSMTP";
        };
    };

    public MailboxParser(String input) {
        super(new CharScanner(input));
    }

    public MailboxParser(CharScanner scanner) {
        super(scanner);
    }

    public MailboxAST parse() throws ParseException {
        MailboxAST mailboxAST = parseMailbox();
        if (currentToken.ch != -1)
            throw currentToken
                    .otherSyntaxException("Superfluous characters after mailbox: {0}");
        return mailboxAST;
    }

    public MailboxAST parseLeft() throws ParseException {
        MailboxAST mailboxAST = parseMailbox();
        scanner.pushBack(currentToken);
        return mailboxAST;
    }

    private MailboxAST parseMailbox() throws ParseException {
        pushPosition();
        pushSpelling();
        LocalPartAST localPartAST = parseLocalPart();
        accept('@');
        RemotePartAST remotePartAST = parseRemotePart();
        return new MailboxAST(popPosition(), popSpelling(), localPartAST,
                remotePartAST);
    }

    private LocalPartAST parseLocalPart() throws ParseException {
        pushPosition();
        pushSpelling();
        if (ATEXT.isSatisfiedBy(currentToken.ch))
            parseDotString();
        else if (currentToken.ch == '"')
            parseQuotedString();
        else
            throw currentToken.syntaxException("Dot-string or Quoted-string");
        return new LocalPartAST(popPosition(), popSpelling());
    }

    private void parseDotString() throws ParseException {
        parseAtom();
        while (currentToken.ch == '.') {
            acceptIt();
            parseAtom();
        }
    }

    private void parseAtom() throws ParseException {
        parseAtext();
        while (ATEXT.isSatisfiedBy(currentToken.ch))
            parseAtext();
    }

    private void parseAtext() throws ParseException {
        if (ATEXT.isSatisfiedBy(currentToken.ch))
            acceptIt();
        else
            throw currentToken.syntaxException("atext");
    }

    private void parseQuotedString() throws ParseException {
        accept('"');
        while (isStarterOfQContentSmtp(currentToken.ch))
            parseQContentSmtp();
        accept('"');
    }

    private void parseQContentSmtp() throws ParseException {
        if (QTEXT_SMTP.isSatisfiedBy(currentToken.ch))
            parseQtextSmtp();
        else if (currentToken.ch == '\\')
            parseQuotedPairSmtp();
        else
            throw currentToken.syntaxException("qtextSMTP or '\\'");
    }

    private boolean isStarterOfQContentSmtp(int ch) {
        return QTEXT_SMTP.isSatisfiedBy(ch) || ch == '\\';
    }

    private void parseQtextSmtp() throws ParseException {
        if (QTEXT_SMTP.isSatisfiedBy(currentToken.ch))
            acceptIt();
        else
            throw currentToken.syntaxException("qtextSMTP");
    }

    private void parseQuotedPairSmtp() throws ParseException {
        accept('\\');
        if (32 <= currentToken.ch && currentToken.ch <= 126)
            acceptIt();
        else
            throw currentToken.syntaxException("any ASCII graphic or space");
    }

    private RemotePartAST parseRemotePart() throws ParseException {
        scanner.pushBack(currentToken);
        RemotePartAST remotePartAST = new RemotePartParser(scanner).parseLeft();
        currentToken = scanner.scan();
        spelling.append(remotePartAST.spelling);
        return remotePartAST;
    }
}
