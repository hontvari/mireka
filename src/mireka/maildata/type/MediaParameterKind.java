package mireka.maildata.type;

import java.util.EnumSet;

import mireka.imap.CiString;

public enum MediaParameterKind {
    Charset("charset"), Boundary("boundary"), Format("format"),
    // special
    Other("<other>");

    public CiString id;

    private MediaParameterKind(String id) {
        this.id = new CiString(id);
    }

    public static MediaParameterKind forName(CiString id) {
        for (MediaParameterKind kind : values()) {
            if (kind.id.equals(id))
                return kind;
        }
        return Other;
    }

    private static final EnumSet<MediaParameterKind> SPECIALS = EnumSet.of(Other);
    public static final EnumSet<MediaParameterKind> KNOWN_PARAMETERS = EnumSet
            .complementOf(SPECIALS);

}
