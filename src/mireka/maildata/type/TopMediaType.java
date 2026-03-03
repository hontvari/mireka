package mireka.maildata.type;

import mireka.imap.CiString;

public enum TopMediaType {
    Text("text"), Image("image"), Audio("audio"), Video("video"), Application("application"),
    Multipart("multipart"), Message("message"),
    /**
     * Unknown top level media type
     */
    Other("<other>");

    public final CiString id;

    TopMediaType(String id) {
        this.id = new CiString(id);
    }

    public static TopMediaType forId(String id) {
        CiString cis = new CiString(id);
        for (TopMediaType t : values()) {
            if (t.id.equals(cis))
                return t;
        }
        return Other;
    }

}
