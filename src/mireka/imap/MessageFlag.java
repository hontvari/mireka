package mireka.imap;

import static mireka.imap.parser.CharClass.ATOM_CHAR;

import java.util.Optional;

import mireka.sieve.CharScanner;

public enum MessageFlag {
    ANSWERED("\\Answered"), FLAGGED("\\Flagged"), DELETED("\\Deleted"), SEEN("\\Seen"),
    DRAFT("\\Draft"),

    MDN_SENT("$MDNSent"), FORWARDED("$Forwarded"), JUNK("$Junk"), NOTJUNK("$NotJunk"),
    PHISHING("$Phishing");

    public final CiString literal;

    MessageFlag(String literal) {
        this.literal = new CiString(literal);
    }

    public CiString ci() {
        return literal;
    }

    @Override
    public String toString() {
        return literal.original;
    }

    public static Optional<MessageFlag> of(CiString name) {
        for (MessageFlag flag : values()) {
            if (flag.literal.equals(name))
                return Optional.of(flag);
        }
        return Optional.empty();
    }

    public static boolean validFlag(String word) {
        CharScanner s = new CharScanner(word);
        boolean isSystemFlag = false;
        if (s.is('\\')) {
            s.takeIt();
            isSystemFlag = true;
        }
        if (!s.is(ATOM_CHAR))
            return false;
        s.takeIt();
        while (s.is(ATOM_CHAR))
            s.takeIt();
        if (!s.isEof())
            return false;
        return !isSystemFlag || MessageFlag.of(new CiString(word)).isPresent();
    }

}
