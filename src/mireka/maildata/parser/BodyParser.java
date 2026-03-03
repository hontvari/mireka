package mireka.maildata.parser;

import java.text.ParseException;

import mireka.maildata.Body;
import mireka.maildata.Entity;
import mireka.maildata.MultipartBody;
import mireka.maildata.ast.BodyNode;
import mireka.maildata.ast.DiscreteBodyNode;
import mireka.maildata.ast.EntityNode;
import mireka.maildata.ast.Fields;
import mireka.maildata.ast.MultipartBodyNode;
import mireka.maildata.io.MaildataInputStream;
import mireka.maildata.type.MediaType;
import mireka.maildata.type.SubMediaType;

/**
 * Parses a MIME or not MIME body of a message. The input is the body of the top level message, and
 * not another entity within a hierarchical MIME message.
 */
public class BodyParser {
    private final Fields topFields;
    private final MaildataInputStream in;

    /**
     * @param topFields the content of the header fields of the message. Only the MIME related
     * fields are used, MIME-Version, Content-Type, Content-Encoding.
     * @param in input stream positioned to the start of the body section
     */
    public BodyParser(Fields topFields, MaildataInputStream in) {
        this.topFields = topFields;
        this.in = in;
    }

    public BodyNode parse() throws ParseException {
        if (topFields.isMime) {
            MediaType mediaType = topFields.mediaType;
            switch (mediaType.type.type) {
            case Text:
            case Image:
            case Audio:
            case Video:
            case Application:
            case Other:
                return parseTopDiscreteBody();
            case Multipart:
                return parseMultipartBody();
            case Message:
                return parseMessageBody();
            default:
                throw new AssertionError();
            }
        } else {
            return parseDiscreteBody(MediaType.TEXT_PLAIN_US_ASCII);
        }
    }

    private DiscreteBodyNode parseTopDiscreteBody() {
        DiscreteBodyNode r = new DiscreteBodyNode();
        r.mediaType = topFields.mediaType;
        r.subsource = in.source;
        return r;
    }

    private DiscreteBodyNode parseDiscreteBody(MediaType mediaType) {
        DiscreteBodyNode r = new DiscreteBodyNode();
        in.skipToEnd();
        r.range = in.range;
        return r;
    }

    private MultipartBodyNode parseMultipartBody(EntityNode entity) {
        MediaType mediaType = entity.simple.getMediaType();
        MultipartBody r = new MultipartBody(mediaType, entity);
        // mediaType.parameters.get()

        // TODO Auto-generated method stub
        r.range = in.range;
        return null;
    }

    /**
     * The nearest entity, which can declare MIME conformity for itself and its childer, either a
     * {@link SubMediaType#MessageRfc822} or a {@link SubMediaType#MessagePartial} entity or the top
     * level message.
     */
    private Entity parentMessage() {
        Entity e = entity;
        while (entity.parent != null && SubMediaType.MIME_VERSION_CONTAINERS
                .contains(entity.simple.getMediaType().type))
            e = e.parent;
        return e;
    }


    private Body parseMessageBody() {
        // TODO Auto-generated method stub
        return null;
    }
}
