package mireka.imap.acl;

import java.util.EnumSet;

/**
 * @see <a href="https://www.rfc-editor.org/rfc/rfc4314.html">RFC 4314 - IMAP4 Access Control List
 * (ACL) Extension</a>
 */
public enum Right {

    LOOKUP('l'), READ('r'), SEEN('s'), WRITE('w'), INSERT('i'), POST('p'), CREATE('k'),
    DELETE_MAILBOX('x'), DELETE_MESSAGE('t'), EXPUNGE('e'), ADMINISTER('a');

    public char ch;

    Right(char c) {
        this.ch = c;
    }

    public static Right forLetter(char ch) {
        for (Right right : values()) {
            if (right.ch == ch)
                return right;
        }
        throw new IllegalArgumentException("Unknown right: " + ch);
    }

    public static String toString(EnumSet<Right> set) {
        StringBuilder b = new StringBuilder();
        for (Right r : set) {
            b.append(r.ch);
        }
        return b.toString();
    }
}