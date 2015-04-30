package mireka.maildata;

import java.util.ArrayList;
import java.util.List;

public class MediaType {
    public static final MediaType MULTIPART_MIXED = new MediaType("multipart",
            "mixed");
    /**
     * The default MIME media type: text/plain; charset=us-ascii.
     */
    public static final MediaType TEXT_PLAIN_US_ASCII = new MediaType("text",
            "plain").addParameter("charset", "us-ascii");

    /**
     * MIME media type (case insensitive)
     */
    public String type;
    /**
     * MIME media subtype (case insensitive)
     */
    public String subtype;

    public List<MediaParameter> parameters = new ArrayList<>();

    public MediaType() {
    }

    public MediaType(String type, String subtype) {
        this.type = type;
        this.subtype = subtype;
    }

    /**
     * Adds the specified media type parameter. It is a convenience function,
     * the effect is the same as calling:
     * 
     * <pre>
     * parameters.add(new MediaParameter(name, value));
     * </pre>
     * 
     * @return this object, following the builder pattern.
     */
    public MediaType addParameter(String name, String value) {
        parameters.add(new MediaParameter(name, value));
        return this;
    }

    /**
     * Returns true if the type and subtype identifiers of this media type equal
     * with the supplied media type.
     */
    public boolean equalTypeIdentifiers(MediaType other) {
        return type.equalsIgnoreCase(other.type)
                && subtype.equalsIgnoreCase(other.subtype);
    }
}
