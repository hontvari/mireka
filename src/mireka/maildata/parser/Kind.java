package mireka.maildata.parser;

import java.util.EnumSet;

import mireka.imap.CiString;
import mireka.maildata.HeaderField;
import mireka.maildata.field.AddressListField;
import mireka.maildata.field.ContentType;
import mireka.maildata.field.DateField;
import mireka.maildata.field.MessageIdField;
import mireka.maildata.field.MimeVersion;
import mireka.maildata.field.UnstructuredField;

public enum Kind {

    // @formatter:off
    // header field names
    BCC("Bcc", AddressListField.class), 
    CC("Cc", AddressListField.class), 
    CONTENT_TYPE("Content-Type", ContentType.class),
    FROM("From", AddressListField.class),
    LIST_ARCHIVE("List-Archive", HeaderField.class), 
    LIST_HELP("List-Help", HeaderField.class),
    LIST_ID("List-Id", HeaderField.class), 
    LIST_OWNER("List-Owner", HeaderField.class),
    LIST_POST("List-Post", HeaderField.class), 
    LIST_SUBSCRIBE("List-Subscribe", HeaderField.class),
    LIST_UNSUBSCRIBE("List-Unsubscribe", HeaderField.class),
    MESSAGE_ID("Message-ID", MessageIdField.class),
    MIME_VERSION("MIME-Version", MimeVersion.class),
    ORIG_DATE("Date", DateField.class),
    RECEIVED("Received", HeaderField.class),
    REPLY_TO("Reply-To", AddressListField.class), 
    RESENT_CC("Resent-Cc", AddressListField.class),
    RESENT_FROM("Resent-From", AddressListField.class), 
    RESENT_TO("Resent-To", AddressListField.class), 
    RETURN_PATH("Return-Path", HeaderField.class),
    SENDER("Sender", AddressListField.class),
    SUBJECT("Subject", UnstructuredField.class), 
    TO("To", AddressListField.class),
    
    // special tokens
    /**
     * A header field which is not of a known type. It will be handled as an unstructured field.  
     */
    OTHER("<OTHER>", HeaderField.class),
    /**
     * An entire header field which contains syntax errors. The header field name may or may not be 
     * determined.
     */
    BADLY_FORMATTED("<BADLY-FORMATTED>", HeaderField.class),
    ;
    
    // @formatter:on

    /**
     * Address list header fields
     */
    public static final EnumSet<Kind> ADDRESS_LISTS = EnumSet.of(BCC, CC, FROM, REPLY_TO, RESENT_CC,
            RESENT_FROM, RESENT_TO, SENDER, TO);
    private static final EnumSet<Kind> SPECIALS = EnumSet.of(OTHER, BADLY_FORMATTED);
    public static final EnumSet<Kind> KNOWN_TYPES = EnumSet.complementOf(SPECIALS);
    public static final EnumSet<Kind> STRUCTURED_FIELDS;
    public static final EnumSet<Kind> UNSTRUCTURED_FIELDS;

    static {
        STRUCTURED_FIELDS = EnumSet.noneOf(Kind.class);
        STRUCTURED_FIELDS.addAll(ADDRESS_LISTS);
        STRUCTURED_FIELDS.add(CONTENT_TYPE);
        STRUCTURED_FIELDS.add(MIME_VERSION);

        UNSTRUCTURED_FIELDS = EnumSet.allOf(Kind.class);
        UNSTRUCTURED_FIELDS.removeAll(STRUCTURED_FIELDS);
        UNSTRUCTURED_FIELDS.remove(BADLY_FORMATTED);
    }

    public CiString name;

    /**
     * @param clazz only for information
     */
    private Kind(String name, Class<? extends HeaderField> clazz) {
        this.name = new CiString(name);
    }

    /**
     * Returns the {@link Kind} instance corresponding to the supplied name, or {@link Kind#OTHER}
     * if not found.
     */
    public static Kind forHeaderFieldName(CiString name) {
        for (Kind kind : values()) {
            if (kind.name.equals(name))
                return kind;
        }
        return OTHER;
    }
}
