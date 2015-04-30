package mireka.maildata;

import mireka.maildata.field.Cc;
import mireka.maildata.field.ContentType;
import mireka.maildata.field.From;
import mireka.maildata.field.MimeVersion;
import mireka.maildata.field.ReplyTo;
import mireka.maildata.field.ResentCc;
import mireka.maildata.field.ResentTo;
import mireka.maildata.field.To;
import mireka.maildata.field.UnstructuredField;
import mireka.util.CharsetUtil;

public class FieldDef<T extends HeaderField> {
// @formatter:off
public static final FieldDef<Cc>                CC               = new FieldDef<>("Cc", Cc.class);
public static final FieldDef<ContentType>       CONTENT_TYPE     = new FieldDef<>("Content-Type", ContentType.class);
public static final FieldDef<From>              FROM             = new FieldDef<>("From", From.class);
public static final FieldDef<HeaderField>       LIST_ARCHIVE     = new FieldDef<>("List-Archive", HeaderField.class);
public static final FieldDef<HeaderField>       LIST_HELP        = new FieldDef<>("List-Help", HeaderField.class);
public static final FieldDef<HeaderField>       LIST_ID          = new FieldDef<>("List-Id", HeaderField.class);
public static final FieldDef<HeaderField>       LIST_OWNER       = new FieldDef<>("List-Owner", HeaderField.class);
public static final FieldDef<HeaderField>       LIST_POST        = new FieldDef<>("List-Post", HeaderField.class);
public static final FieldDef<HeaderField>       LIST_SUBSCRIBE   = new FieldDef<>("List-Subscribe", HeaderField.class);
public static final FieldDef<HeaderField>       LIST_UNSUBSCRIBE = new FieldDef<>("List-Unsubscribe", HeaderField.class);
public static final FieldDef<MimeVersion>       MIME_VERSION     = new FieldDef<>("MIME-Version", MimeVersion.class);
public static final FieldDef<HeaderField>       RECEIVED         = new FieldDef<>("Received", HeaderField.class);
public static final FieldDef<ReplyTo>           REPLY_TO         = new FieldDef<>("Reply-To", ReplyTo.class);
public static final FieldDef<ResentCc>          RESENT_CC        = new FieldDef<>("Resent-Cc", ResentCc.class);
public static final FieldDef<ResentTo>          RESENT_TO        = new FieldDef<>("Resent-To", ResentTo.class);
public static final FieldDef<HeaderField>       RETURN_PATH      = new FieldDef<>("Return-Path", HeaderField.class);
public static final FieldDef<UnstructuredField> SUBJECT          = new FieldDef<>("Subject", UnstructuredField.class);
public static final FieldDef<To>                TO               = new FieldDef<>("To", To.class);
// @formatter:on

    private final String fancyName;
    private final String lowerCaseName;
    private final Class<T> clazz;

    public FieldDef(String fancyName, Class<T> clazz) {
        this.fancyName = fancyName;
        this.lowerCaseName = CharsetUtil.toAsciiLowerCase(fancyName);
        this.clazz = clazz;
    }

    public String fancyName() {
        return fancyName;
    }

    public String lowerCaseName() {
        return lowerCaseName;
    }

    public Class<T> clazz() {
        return clazz;
    }

    /**
     * Returns {@link #fancyName()}
     */
    public String toString() {
        return fancyName;
    }

}
