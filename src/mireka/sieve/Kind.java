package mireka.sieve;

import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Optional;

import mireka.imap.CiString;
import mireka.sieve.Interpreter.KeywordToken;
import mireka.sieve.Interpreter.NumberToken;
import mireka.sieve.Interpreter.SeparatorToken;
import mireka.sieve.Interpreter.StringToken;
import mireka.sieve.Interpreter.Token;

public enum Kind {
    //@formatter:off
    // generic token types specified by the RFC
    Number(NumberToken.class, "<number>"),
    QuotedString(StringToken.class, "<quoted-string>"),
    Multiline(StringToken.class, "<multiline-string>"),

    // commands - class is KeywordToken
    // commands/control
    If(KeywordToken.class, "if"), 
    Elsif(KeywordToken.class, "elsif"),
    Else(KeywordToken.class, "else"), 
    Require(KeywordToken.class, "require"),
    Stop(KeywordToken.class, "stop"),
    // commands/action
    Fileinto(KeywordToken.class, "fileinto"), 
    Redirect(KeywordToken.class, "redirect"),
    Keep(KeywordToken.class, "keep"), 
    Discard(KeywordToken.class, "discard"),
    Setflag(KeywordToken.class, "setflag"),
    Addflag(KeywordToken.class, "addflag"),
    Removeflag(KeywordToken.class, "removeflag"),
    // commands/test
    Address(KeywordToken.class, "address"), 
    Allof(KeywordToken.class, "allof"),
    Anyof(KeywordToken.class, "anyof"), 
    Envelope(KeywordToken.class, "envelope"),
    Exists(KeywordToken.class, "exists"), 
    False(KeywordToken.class, "false"),
    Header(KeywordToken.class, "header"), 
    Not(KeywordToken.class, "not"),
    Size(KeywordToken.class, "size"), 
    True(KeywordToken.class, "true"),

    // tags
    // tags/COMPARATOR
    Comparator(KeywordToken.class, ":comparator"),
    // tag/MATCH-TYPE
    Is(KeywordToken.class, ":is"), 
    Contains(KeywordToken.class, ":contains"),
    Matches(KeywordToken.class, ":matches"),
    // tag/ADDRESS-PART
    Localpart(KeywordToken.class, ":localpart"), 
    Domain(KeywordToken.class, ":domain"),
    All(KeywordToken.class, ":all"),
    // tag/others
    Flags(KeywordToken.class, ":flags"),

    // separators
    LeftParenthesis(SeparatorToken.class, "("), RightParenthesis(SeparatorToken.class, ")"),
    LeftBracket(SeparatorToken.class, "["), RightBracket(SeparatorToken.class, "]"),
    LeftBrace(SeparatorToken.class, "{"), RightBrace(SeparatorToken.class, "}"),
    Comma(SeparatorToken.class, ","), 
    Semicolon(SeparatorToken.class, ";"),

    // special tokens
    Eof(Token.class, "Eof");
    // @formatter:on

    public final CiString spelling;

    /**
     * @param clazz only for information
     */
    private Kind(Class<? extends Token> clazz, String spelling) {
        this.spelling = new CiString(spelling);
    }

    public static EnumSet<Kind> COMMAND = EnumSet.of(If, Require, Stop, Fileinto, Redirect, Keep,
            Discard, Setflag, Addflag, Removeflag);
    public static EnumSet<Kind> MATCH_TYPE = EnumSet.of(Is, Contains, Matches);
    public static EnumSet<Kind> ADDRESS_PART = EnumSet.of(Localpart, Domain, All);

    public static Optional<Kind> fromSpelling(byte[] spellingBytes) {
        CiString spelling = new CiString(new String(spellingBytes, StandardCharsets.UTF_8));
        for (Kind kind : values()) {
            if (kind.spelling.equals(spelling))
                return Optional.of(kind);
        }
        return Optional.empty();
    }
}
