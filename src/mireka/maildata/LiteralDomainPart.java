package mireka.maildata;

public class LiteralDomainPart implements DomainPart {

    /**
     * The domain literal without the enclosing square brackets. Note that in
     * its obsolete form it may contain escaped square brackets, so this string
     * must be escaped when it is written into mail data.
     */
    public String literal;

    public LiteralDomainPart(String literal) {
        this.literal = literal;
    }
}
