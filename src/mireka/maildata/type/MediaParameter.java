package mireka.maildata.type;

import mireka.imap.CiString;

/**
 * MediaParameter provides auxiliary information to the media type and subtype
 * identifiers.
 */
public class MediaParameter {
    public MediaParameterKind kind;
    /**
     * Parameter name (case-insensitive)
     */
    public CiString name;

    public String value;

    public MediaParameter(MediaParameterKind kind) {
        this.kind = kind;
        this.name = kind.id;
    }

    public MediaParameter(MediaParameterKind kind, String value) {
        this.kind = kind;
        this.name = kind.id;
        this.value = value;
    }

    public MediaParameter(String name, String value) {
        this.name = new CiString(name);
        this.value = value;
        this.kind = MediaParameterKind.forName(this.name);
    }
}
