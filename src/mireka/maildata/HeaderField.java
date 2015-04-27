package mireka.maildata;

import static mireka.util.CharsetUtil.*;

import java.io.IOException;
import java.io.OutputStream;

public abstract class HeaderField {

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

    public HeaderField() {
        super();
    }

    public HeaderField(String name) {
        setName(name);
    }

    /**
     * Field name in lower case, or null if the name cannot be determined.
     */
    public String lowerCaseName;

    public void setName(String name) {
        this.name = name;
        this.lowerCaseName = name == null ? null : toAsciiLowerCase(name);
    }

    public void writeTo(OutputStream out) throws IOException {
        if (source != null)
            out.write(toAsciiBytes(source.originalSpelling));
        else
            writeGenerated(out);
    }

    protected abstract void writeGenerated(OutputStream out) throws IOException;
}