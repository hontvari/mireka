package mireka.imap.command;

import static mireka.imap.Completion.OK;
import static mireka.imap.SessionState.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import mireka.imap.store.MailboxHierarchy;
import mireka.imap.store.MailboxName;
import mireka.imap.store.Node;

public class ListCommand extends Command {
    private static final Set<String> SELECT_BASE_OPTS = Set.of("SUBSCRIBED");
    private static final Set<String> SELECT_MOD_OPTS = Set.of("RECURSIVEMATCH");
    /**
     * See RFC 9051
     */
    private boolean isExtendedSyntax;
    private String reference;
    private String pattern;
    /**
     * 0 or 1 canonical pattern which is constructed from the {@link #reference} and the
     * {@link #pattern}. In the extended syntax an empty mailbox pattern is ignored.
     */
    private List<String> canonicalPatters = new ArrayList<>();
    // private static final Set<String> SELECT_INDEPENDENT_OPTS = Set.of("REMOTE");

    private Set<String> selectOptions = new HashSet<>();
    private boolean isSelectSubscribed, isSelectRecursiveMatch, isSelectRemote;
    private boolean isReturnSubscribed, isReturnChildren;
    private boolean isReturnStatusMessages, isReturnStatusUidnext, isReturnStatusUidvalidity,
            isReturnStatusUnseen, isReturnStatusDeleted, isReturnStatusSize;

    private Set<MailboxName> set = new HashSet<>();
    private Map<Namespace, MailboxHierarchy> storeHierarchy = new HashMap<>();
    private Map<Namespace, MailboxHierarchy> subscriptionHierarchy = new HashMap<>();

    public ListCommand(Session session, CommandParser parser) {
        super(session, parser);
    }

    @Override
    public void parse() throws IOException, ImapException {
        take(' ');
        if (parser.next() == '(') {
            isExtendedSyntax = true;
            parseListSelectOpts();
            take(' ');
        }
        reference = parser.parseAstring("mailbox");
        take(' ');
        parseMboxOrPat();
        if (parser.isSpace()) {
            take();
            isExtendedSyntax = true;
            parseListReturnOpts();
        }
    }

    private void parseListSelectOpts() throws IOException, CommandSyntaxException {
        parser.take(parser.next() == '(', "open parenthesis");
        if (parser.isAtomChar()) {
            parseSelectOpt();
            while (parser.isSpace()) {
                parser.take();
                parseSelectOpt();
            }
        }
        parser.take(parser.next() == ')', "close parenthesis");
    }

    private void parseSelectOpt() throws CommandSyntaxException, IOException {
        String name = parser.parseKeyword("list-select-opt");
        switch (name) {
        case "SUBSCRIBED":
            isSelectSubscribed = true;
            break;
        case "REMOTE":
            isSelectRemote = true;
            break;
        case "RECURSIVEMATCH":
            isSelectRecursiveMatch = true;
        default:
            throw new CommandSyntaxException("Unknown list-select-opt: " + name);
        }
        selectOptions.add(name);
    }

    private static boolean containsAny(Set<String> s1, Set<String> s2) {
        for (String s : s2) {
            if (s1.contains(s))
                return true;
        }
        return false;
    }

    private void parseMboxOrPat() throws CommandSyntaxException, IOException {
        if (parser.isListChar() || parser.isDquote() || parser.next() == '{') {
            pattern = parseListMailbox();
        } else if (parser.next() == '(') {
            isExtendedSyntax = true;
            parsePatterns();
        } else {
            throw new CommandSyntaxException(parser.formatCommandException("mbox-or-pat"));
        }
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

    private void parsePatterns() throws CommandSyntaxException, IOException {
        parser.take(parser.next() == '(', "patterns");
        pattern = parseListMailbox();
        parser.take(parser.next() == ')', "patterns");

    }

    private void parseListReturnOpts() throws CommandSyntaxException, IOException {
        String name = parser.parseKeyword("RETURN");
        if (!name.equals("RETURN"))
            throw new CommandSyntaxException(parser.formatCommandException("RETURN"));
        parser.take(parser.isSpace(), "space");
        parser.take(parser.next() == '(', "open parenthesis");
        if (parser.isAtomChar()) {
            parseReturnOption();

        }
        parser.take(parser.next() == ')', "close parenthesis");
    }

    private void parseReturnOption() throws CommandSyntaxException, IOException {
        String name = parser.parseKeyword("list-return-opt");
        switch (name) {
        case "SUBSCRIBED":
            isReturnSubscribed = true;
            break;
        case "CHILDREN":
            isReturnChildren = true;
            break;
        case "STATUS":
            parseReturnStatus();
            break;
        default:
            throw new CommandSyntaxException("Unknown list-return-opt: " + name);
        }
    }

    /**
     * STATUS keyword is already parsed
     */
    private void parseReturnStatus() throws CommandSyntaxException, IOException {
        parser.take(parser.isSpace(), "space");
        parser.take(parser.next() == '(', "open parenthesis");
        if (parser.isAtomChar()) {
            parseStatusAtt();
            while (parser.isSpace()) {
                parser.take();
                parseStatusAtt();
            }
        }
        parser.take(parser.next() == ')', "close parenthesis");
    }

    private void parseStatusAtt() throws CommandSyntaxException, IOException {
        String name = parser.parseKeyword("status-att");
        switch (name) {
        case "MESSAGES":
            isReturnStatusMessages = true;
            break;
        case "UIDNEXT":
            isReturnStatusUidnext = true;
            break;
        case "UIDVALIDITY":
            isReturnStatusUidvalidity = true;
            break;
        case "UNSEEN":
            isReturnStatusUnseen = true;
            break;
        case "DELETED":
            isReturnStatusDeleted = true;
            break;
        case "SIZE":
            isReturnStatusSize = true;
            break;
        default:
            throw new CommandSyntaxException("Unknown status-att: " + name);
        }
    }

    @Override
    public Completion execute() throws IOException, ImapException {
        if (containsAny(selectOptions, SELECT_MOD_OPTS)
                && !containsAny(selectOptions, SELECT_BASE_OPTS))
            throw new CommandSyntaxException(
                    "A select-mod-option is present, but a select-base-opt is not");

        if (!isExtendedSyntax && pattern.isEmpty()) {
            // This combination has a special meaning, see RFC.
            sendNamespaceInfo();
        } else {
            sendRealList();
        }

        return OK;
    }

    /**
     * Send a response with the delimiter and prefix corresponding to the reference, this is a
     * special mode of the LIST command.
     */
    protected void sendNamespaceInfo() throws IOException {
        MailboxName mb = repository().parseMailboxName(session.user, reference);
        MailboxName root = repository().parseMailboxName(session.user, mb.namespace.prefix);
        EnumSet<MailboxFlag> flags = hierarchy(mb.namespace).mailboxAttributes(root);
        out.respondList(flags, mb.namespace.prefix, new ParenthesizedList());
    }

    /**
     * Sends the list of mailbox names corresponding to the pattern, this is the normal mode of the
     * LIST command.
     */
    protected void sendRealList() throws IOException {
        list();
        List<MailboxName> list = new ArrayList<>(set);
        list.sort(null);
        for (MailboxName mbname : list) {
            EnumSet<MailboxFlag> flags = storeHierarchy(mbname.namespace).mailboxAttributes(mbname);
            if (subscriptionHierarchy(mbname.namespace).queryNode(mbname) != null)
                flags.add(MailboxFlag.SUBSCRIBED);
            out.respondList(flags, mbname.original, new ParenthesizedList());
        }
    }

    private void list() {
        if (pattern.isEmpty())
            return;
        canonicalPatters.add(mergeReferenceAndPattern());
        for (String canPattern : canonicalPatters) {
            listForPattern(canPattern);
        }
    }

    private void listForPattern(String canPattern) {
        Namespace namespace = repository().namespaces().forName(canPattern);
        Pattern pattern = patternToRegexp(canPattern);
        MailboxHierarchy src;
        if (isSelectSubscribed)
            src = subscriptionHierarchy(namespace);
        else
            src = storeHierarchy(namespace);

        for (String name : src.findNames(pattern)) {
            MailboxName mbname = repository().parseMailboxName(session.user, name);
            Node node = src.queryNode(mbname);
            if (isSelectSubscribed) {
                if (node.isExpicit || (isSelectRecursiveMatch && node.isAnyRecursive(n -> true)))
                    set.add(mbname);
            } else {
                set.add(mbname);
            }
        }
    }

    /**
     * It assumes that {@link #pattern} is not empty.
     */
    private String mergeReferenceAndPattern() {
        if (reference.isEmpty()) {
            return pattern;
        } else {
            Namespace ns = repository().namespaces().forName(reference);
            if (reference.equals(ns.prefix))
                return reference + pattern;
            else if (reference.endsWith(ns.delimiter))
                return reference + pattern;
            else
                return reference + ns.delimiter + pattern;
        }
    }

    private MailboxHierarchy storeHierarchy(Namespace ns) {
        return storeHierarchy.computeIfAbsent(ns,
                n -> repository().userMailboxHierarchy(session.user, n));
    }

    private MailboxHierarchy subscriptionHierarchy(Namespace ns) {
        return subscriptionHierarchy.computeIfAbsent(ns,
                n -> repository().subsribedMailboxHierarchy(session.user, n));
    }

    private static Pattern patternToRegexp(String pattern) {
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
