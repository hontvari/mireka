package mireka.maildata.ast;

import javax.annotation.Nullable;

import mireka.maildata.io.Subsource;

/**
 * Represents a complete RFC822/RFC5322 message. The body is simply an unstructured character
 * stream, not a MIME structured body.
 * 
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc5322#section-3.5">Overall Message
 * Syntax</a>
 */
public class MessageNode {
    public Subsource source;

    public Fields fields;
    public boolean hasSeparator;
    public Subsource fieldsAndSeparatorSource;
    @Nullable
    public Subsource bodySource;

}
