package mireka.maildata;

/**
 * MediaParameter provides auxiliary information to the media type and subtype
 * identifiers.
 */
public class MediaParameter {
    /**
     * Parameter name (case-insensitive)
     */
    public String name;

    public String value;

    public MediaParameter() {
    }

    public MediaParameter(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
