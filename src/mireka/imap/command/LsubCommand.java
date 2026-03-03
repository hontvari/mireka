package mireka.imap.command;

import static mireka.imap.Completion.OK;
import static mireka.imap.SessionState.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;

import mireka.imap.Completion;
import mireka.imap.ImapException;
import mireka.imap.MailboxFlag;
import mireka.imap.Namespace;
import mireka.imap.ParenthesizedList;
import mireka.imap.Session;
import mireka.imap.SessionState;
import mireka.imap.parser.CommandParser;
import mireka.imap.parser.CommandSyntaxException;
import mireka.imap.store.MailboxName;

/**
 * This is an obsolete command as of IMAP 4rev2
 */
public class LsubCommand extends Command {
    private String reference;
    private String pattern;

    public LsubCommand(Session session, CommandParser parser) {
        super(session, parser);
    }

    @Override
    public void parse() throws IOException, ImapException {
        take(' ');
        reference = parser.parseAstring("mailbox");
        take(' ');
        pattern = parseListMailbox();
    }

    private String parseListMailbox() throws IOException, CommandSyntaxException {
        if (parser.isListChar()) {
            try (CommandParser.Level l = parser.beginLevel("listchars")) {
                while (parser.isListChar()) {
                    parser.take();
                }
                return l.spelling();
            }
        } else if (parser.isDquote() || parser.next() == '{') {
            return parser.parseString();
        } else {
            throw new CommandSyntaxException(parser.formatCommandException("list-mailbox"));
        }
    }

    @Override
    public Completion execute() throws IOException, ImapException {
        for (MailboxName mbname : list()) {
            EnumSet<MailboxFlag> flags = hierarchy(mbname.namespace).mailboxAttributes(mbname);
            out.respondLsub(flags, mbname.original, new ParenthesizedList());
        }

        return OK;
    }

    private List<MailboxName> list() {
        List<MailboxName> r = new ArrayList<>();
        Pattern pattern = patternToRegexp();
        Namespace namespace = repository().namespaces().forName(this.pattern);
        for (String name : repository().subsribedMailboxHierarchy(session.user, namespace).map
                .keySet()) {
            if (pattern.matcher(name).matches()) {
                MailboxName mbname = repository().parseMailboxName(session.user, name);
                r.add(mbname);
            }
        }
        r.sort(null);
        return r;
    }

    private Pattern patternToRegexp() {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < pattern.length(); i++) {
            char ch = pattern.charAt(i);
            switch (ch) {
            case '\\':
            case '[':
            case ']':
            case '{':
            case '}':
            case '^':
            case '$':
            case '?':
            case '+':
            case '|':
            case '(':
            case ')':
            case '.':
                // escape regular expression special characters
                buffer.append('\\');
                buffer.append(ch);
                break;
            case '%':
                buffer.append("[^/]*");
                break;
            case '*':
                buffer.append(".*");
                break;
            default:
                buffer.append(ch);
            }
        }
        return Pattern.compile(buffer.toString());
    }

    @Override
    public Completion asyncExecute() throws IOException, ImapException {
        throw new IllegalStateException();
    }

    @Override
    public EnumSet<SessionState> allowed() {
        return EnumSet.of(AUTHENTICATED, SELECTED);
    }

}
