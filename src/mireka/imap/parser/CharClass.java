package mireka.imap.parser;

import java.util.function.Predicate;

public class CharClass {
    public static final CharClass DOT = new CharClass('.');
    public static final CharClass DQUOTE = new CharClass('"');
    public static final CharClass SPACE = new CharClass(' ');

    public static final CharClass CTL = new CharClass("CTL",
            c -> 0x00 <= c && c <= 0x1F || c == 0x7F);
    public static final CharClass CHAR = new CharClass("CHAR", c -> 0x01 <= c && c <= 0x7F);

    public static final CharClass QUOTED_SPECIALS = new CharClass("quoted-specials",
            c -> DQUOTE.test(c) || c == '\\');
    public static final CharClass DIGIT_NZ = new CharClass("list-wildcard",
            c -> 0x31 <= c && c <= 0x39);
    public static final CharClass LIST_WILDCARD = new CharClass("list-wildcard",
            c -> c == '%' || c == '*');
    public static final CharClass RESP_SPECIALS = new CharClass("resp-specials", c -> c == ']');
    public static final CharClass ATOM_SPECIALS = new CharClass("ATOM-SPECIALS",
            c -> c == '(' || c == ')' || c == '{' || SPACE.test(c) || CTL.test(c)
                    || LIST_WILDCARD.test(c) || QUOTED_SPECIALS.test(c)
                    || RESP_SPECIALS.test(c));
    public static final CharClass ATOM_CHAR = new CharClass("ATOM-CHAR",
            c -> CHAR.test(c) && !ATOM_SPECIALS.test(c));

    String name;
    Predicate<Integer> test;

    public CharClass(String name, Predicate<Integer> test) {
        this.test = test;
        this.name = name;
    }

    public CharClass(char ch) {
        this.name = "\"" + ch + "\"";
        this.test = x -> x == ch;
    }

    public boolean test(int c) {
        return test.test(c);
    }
}