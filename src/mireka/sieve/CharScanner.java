package mireka.sieve;

import mireka.imap.parser.CharClass;

public class CharScanner {
    private static final int EOF = -1;
    private String value;
    public int pos = 0;

    /**
     * This is part of the upper level parser.
     */
    public int next;

    public CharScanner(String s) {
        this.value = s;
        scan();
    }

    public int scan() {
        next = pos == value.length() ? EOF : value.charAt(pos++);
        return next;
    }

    /**
     * This is part of the upper level parser.
     */
    public int takeIt() {
        int current = next;
        next = scan();
        return current;
    }

    public boolean is(char c) {
        return next == c;
    }

    public boolean is(CharClass c) {
        return c.test(next);
    }

    public boolean isEof() {
        return next == EOF;
    }

}