package mireka.maildata;

import static mireka.maildata.Entity.EditStatus.UPDATED;
import static mireka.maildata.Entity.ParsingStatus.*;
import static mireka.util.CharsetUtil.toAsciiBytes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import javax.annotation.Nullable;

import mireka.maildata.io.MaildataSource;
import mireka.maildata.io.PositionOutputStream;
import mireka.maildata.io.Range;
import mireka.maildata.io.Subsource;
import mireka.maildata.parser.MaildataParser;
import mireka.util.StreamCopier;

/**
 * A MIME entity, the MIME-defined header fields and contents of either a message or one of the
 * parts in the body of a multipart entity.
 * 
 * It has 4 states:
 * <li>constructed - from existing message</li>
 * <li>parsed - only header of the full entity</li>
 * <li>updated - updated existing message or new message</li>
 * <li>saved</li>
 */
public class Entity implements AutoCloseable {
    private static final byte[] CRLF = new byte[] { '\r', '\n' };
    public Entity parent;
    private ParsingStatus parsingStatus;
    private EditStatus editStatus;

    /**
     * Its source may be an empty string, although in case of a message that is semantically
     * invalid, in case of a body part it may be semantically valid.
     * 
     * Null if this entity represents an existing entity but the header section is not parsed yet.
     * It is never null in a new entity, which has no {@link #source}. It is never null if the body
     * is not null.
     */
    private HeaderSection header;

    /**
     * True if there is a separator line after the header. False also means that there is no body in
     * this entity. It is set when the header section is parsed, so this field is only valid if
     * {@link #header} is not null.
     */
    private boolean hasSeparatorLine;
    /**
     * Null if body is not yet parsed or if the body is missing.
     */
    private Body body;

    /**
     * false, if this object represents a new mail, initially with empty data, instead of an
     * existing mail read from a source.
     */
    // private final boolean isExisting;
    /**
     * Points to the serialized, byte stream form of this entity. This form is either generated from
     * a new or updated message, or come from an already existing message received in this form.
     * This field is either set during parsing or saving the message. It is only valid if
     * {@link #isUpdated() is false.
     * 
     * Non null if the body comes from an existing source or if it is already saved, otherwise null.
     */
    @Nullable
    protected Subsource generated;
    /**
     * It is valid is {@link #isExisting} true.
     */
    // private boolean isHeaderParsed;
    /**
     * It is valid is {@link #isExisting} true.
     */
    // private boolean isFullyParsed;

    /**
     * True if this object itself changed, not including changes is in subobjects. In this object
     * the only way to change this object itself to create a new body instead of a non-existing one.
     * True means that this object deviated from {@link #source}. This field is only valid if
     * {@link #source} is not null.
     */
    private boolean isUpdated;

    public final SimpleMaildata simple = new SimpleMaildata(this);

    /**
     * Creates a new mail which is initially empty.
     */
    public Entity() {
        // isExisting = false;
        parsingStatus = PARSED;
        editStatus = UPDATED;
        generated = null;
        header = new HeaderSection();
    }

    /**
     * Creates a new object which will represent the MIME Entity in the supplied
     * {@link MaildataSource}.
     * 
     * @param source the {@link MaildataSource} which will be parsed by this object on demand. It
     * will be closed by the close method of this object. It is guaranteed that this constructor
     * does not start to read this sourceFile. The content of the source may be initialized later,
     * but it must be initialized before a call to any other method of this instance.
     */
    public Entity(Subsource source) {
        // isExisting = true;
        parsingStatus = UNPARSED;
        editStatus = EditStatus.UNCHANGED;
        this.generated = source;
    }

    /**
     * Returns the header section after parsing the message structure if necessary. It does not
     * parse the body, this may be efficient if the body is not required. It parses the header
     * fields but only partially, it retrieves the field name and the unstructured content of the
     * field body but it does not parse the semantic content of the field body.
     */
    public HeaderSection headers() {
        if (parsingStatus == UNPARSED) {
            MaildataParser parser = new MaildataParser(generated.getInputStream());
            parser.parseHeader(header);
            hasSeparatorLine = parser.parseSeparator();
            isHeaderParsed = true;
        }
        return header;
    }

    public Optional<Body> body() {
        if (isExisting && !isFullyParsed) {
            // make sure the header section is parsed
            headers();

        }
        return Optional.ofNullable(body);
    }

    public Range headerAndSeparatorRange() {
        if (headers().isUpdated)
            throw new IllegalStateException();
        Range range = headers().range();
        return new Range(range.start, range.length + (hasSeparatorLine ? 2 : 0));
    }

    public MailMap structure() {
        if (isExisting && !isFullyParsed) {
            if (parser == null)
                parser = new MaildataParser(source.getInputStream());
            if (isHeaderParsed) {
                if (map.separator != null)
                    parser.parseBodyAndEof(map);
            } else {
                parser.parse(map);
            }
            isFullyParsed = true;
            isHeaderParsed = true;
        }
        return map;
    }

    public void writeTo(OutputStream out) throws IOException {
        if (isUpdated()) {
            writeUpdatedTo(out);
        } else {
            try (InputStream in = source.getInputStream()) {
                StreamCopier.writeInputStreamIntoOutputStream(in, out);
            }
        }
    }

    private void registerUpdate() {
        switch (editStatus) {
        case UNCHANGED:
            editStatus = UPDATED;
            if (parent != null)
                parent.registerUpdate();
            break;
        case UPDATED:
            break;
        default:
            throw new AssertionError();
        }
    }

    /**
     * Returns true if some part of this mail data has been updated, indicating that {@link #source}
     * does not reflect the current state. False if this object comes from an existing message,
     * which is not updated, or from a new or updated message which is saved.
     */
    public boolean isUpdated() {
        if (source == null)
            return true;
        return isUpdated || (header != null ? header.isUpdated : false)
                || (body != null && body.isUpdated());
    }

    private void writeUpdatedTo(OutputStream out) throws IOException {
        header.writeTo(out);
        if (body != null || hasSeparatorLine)
            out.write(toAsciiBytes("\r\n"));
        if (body != null)
            body.writeTo(out);
    }

    protected void save(PositionOutputStream out) {
        headers().save(out);
        if (body().isPresent()) {
            hasSeparatorLine = true;
            out.write(CRLF);
            body().save(out);
        } else {
            hasSeparatorLine = false;
        }
    }

    /**
     * Returns an input stream with the content of this entity. The entity must be in a saved state,
     * which means that {@link #isUpdated()} must be false.
     */
    public InputStream getInputStream() {
        if (isUpdated())
            throw new IllegalStateException();
        return generated.getInputStream();
    }

    /**
     * Releases system resources associated with this object.
     */
    @Override
    public void close() {
        if (body != null)
            body.close();
    }
    
    public enum ParsingStatus {
        /**
         * from existing message
         */
        UNPARSED, HEADER_PARSED,
        /**
         * after saving an {@link #UPDATED} entity, this becomes the state again.
         */
        PARSED,
    }

    public enum EditStatus {
        /**
         * updated existing message or new message
         */
        UNCHANGED, UPDATED,
        // Trace headers may be prepended to the message without changing anything else
        // PREPENDED
        ;

    }
}
