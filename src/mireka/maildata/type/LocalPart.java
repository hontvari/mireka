package mireka.maildata.type;

import javax.annotation.Nullable;

import mireka.maildata.parser.FieldGenerator;

/**
 * Local part of an email address.
 * 
 * @see "local-part of addr-spec in RFC 5322."
 */
public class LocalPart {
    @Nullable
    public String spelling;
    /**
     * The semantic content of the local part. For example if it was specified as a quoted-string
     * the mail header field, then quotes are not included in this string.
     */
    public String value;

    public LocalPart(String value) {
        this.value = value;
    }

    public String generate() {
        FieldGenerator g = new FieldGenerator();
        g.writeLocalPart(this);
        return g.toString();
    }
}
