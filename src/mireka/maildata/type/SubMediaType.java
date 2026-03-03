package mireka.maildata.type;

import static mireka.maildata.type.TopMediaType.*;

import java.util.EnumSet;

import mireka.imap.CiString;

public enum SubMediaType {
    // discrete types
    TextPlain(Text, "plain"),
    // composite/multiparts
    MultipartMixed(Multipart, "mixed"), MultipartAlternative(Multipart, "alternative"),
    // message
    MessageRfc822(Message, "rfc822"), MessagePartial(Message, "partial"),
    
    TextOther(Text, "<other>"),
    AudioOther(Audio, "<other>"), 
    VideoOther(Video, "<other>"), 
    ApplicationOther(Application, "<other>"), 
    MultipartOther(Multipart, "<other>;"), 
    MessageOther(Message, "<other>"), 
    OtherOther(Other, "<other>"),
    ;
    
    public final TopMediaType type;
    public final CiString id;
    
    private SubMediaType(TopMediaType type, String id) {
        this.type = type;
        this.id = new CiString(id);
    }

    public static SubMediaType valueOf(String top, String sub) {
        TopMediaType type = TopMediaType.forId(top);

        CiString ciid = new CiString(sub);
        for (SubMediaType st : values()) {
            if (st.type == type && st.id.equals(ciid))
                return st;
        }
        switch (type) {
        case Text:
            return TextOther;
        case Audio:
            return AudioOther;
        case Video:
            return VideoOther;
        case Application:
            return ApplicationOther;
        case Multipart:
            return MultipartOther;
        case Other:
            return OtherOther;
        default:
            throw new AssertionError();
        }
    }

    /**
     * Top level media types which can include the MIME-Version header, not including the top level
     * entity.
     */
    public static final EnumSet<SubMediaType> MIME_VERSION_CONTAINERS = EnumSet.of(MessageRfc822,
            MessagePartial);
}
