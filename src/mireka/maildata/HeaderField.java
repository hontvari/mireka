package mireka.maildata;

import java.io.IOException;

import javax.annotation.Nullable;

import mireka.maildata.field.UnstructuredField;
import mireka.maildata.parser.Kind;

public abstract class HeaderField {
    public final Kind kind;

    /**
     * Null if the header is newly created, instead of being extracted from a
     * received mail. It is also null if the body has been updated.
     */
    public HeaderFieldText source;

    /**
     * Field name, or null if the name cannot be determined. The case when it
     * cannot be determined only occurs if the header field is syntactically
     * invalid.
     */
    public String name;

    /**
     * Unfolded single line, without CRLF, it may contain non-ASCII characters, leading and ending
     * whitespace is trimmed.
     * 
     * The field body parsed as an unstructured field. This is useful for generic search
     * functionality and for fields which are unstructured by nature, like Subject. If the body has
     * syntactic errors, than this contains the unparsed text. It may be null, if the field has
     * syntactic error which prevents separating the field name and body.
     * 
     * Note that {@link #bodyFull} has higher priority when writing out this field into a stream.
     * 
     * @see UnstructuredField
     */
    public String body;
    /**
     * Same as {@link #body}, but leading and ending whitespace is not trimmed. It can be null, if
     * this is a newly generated field, and it is not set by the client program. In that case it is
     * assumed that the full body is the concatenated value of a single space and the {@link #body
     * field.}
     * 
     * Conventionally there is a space before the actual text, so the heading looks better, even
     * though this is somewhat wrong, because the space becomes part of the semantic value.
     */
    @Nullable
    public String bodyFull;

    /**
     * Convenience method for modifying the content of the existing field. It sets both
     * {@link #body} and {@link #bodyFull}. The new value body is the supplied field, the new value
     * of bodyFull is a space character plus the supplied field.
     * 
     * @param trimmedBody the body to be set, without leading space.
     */
    public void setBody(String trimmedBody) {
        this.body = trimmedBody;
        this.bodyFull = " " + trimmedBody;
    }

    public HeaderField(Kind kind) {
        this.kind = kind;
        this.name = kind.name.original;
    }

    protected abstract HeaderFieldText generate() throws IOException;
}