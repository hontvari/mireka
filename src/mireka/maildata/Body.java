package mireka.maildata;

import java.io.OutputStream;

import javax.annotation.Nullable;

import mireka.maildata.io.MaildataSource;
import mireka.maildata.io.Range;
import mireka.maildata.type.MediaType;

/**
 * Body of either a message or of a MIME body part.
 */
public abstract class Body {
    public final MediaType kind;

    /**
     * The entity whose content is this body.
     */
    public Entity owner;

    /**
     * The position of this body in the source mail stream. It is null, if this body is contained by
     * a new message.
     */
    @Nullable
    public Range range;

    public Body(MediaType kind, Entity owner) {
        this.kind = kind;
        this.owner = owner;
    }

    /**
     * It is given if the body comes from an existing source, otherwise null.
     */
    MaildataSource source;

    public abstract boolean isUpdated();

    public abstract void writeTo(OutputStream out);

    protected abstract void close();
}
