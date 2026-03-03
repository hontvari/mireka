package mireka.maildata.type;

import static mireka.maildata.type.MediaParameterKind.*;
import static mireka.maildata.type.SubMediaType.TextPlain;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import mireka.imap.CiString;
import mireka.maildata.parser.StructuredFieldBodyParser;

public class MediaType {
    /**
     * The default MIME media type: text/plain; charset=us-ascii.
     */
    public static final MediaType TEXT_PLAIN_US_ASCII = new MediaType(TextPlain)
            .setParameter(Charset, "us-ascii");

    public SubMediaType type;
    /**
     * MIME media type (case insensitive)
     */
    public CiString topTypeName;
    /**
     * MIME media subtype (case insensitive)
     */
    public CiString subtypeName;

    private List<MediaParameter> parameters = new ArrayList<>();

    public MediaType(SubMediaType type) {
        this.type = type;
        this.topTypeName = type.type.id;
        this.subtypeName = type.id;
    }

    public MediaType(String toptypeName, String subtypeName) {
        this.type = SubMediaType.valueOf(toptypeName, subtypeName);
        this.topTypeName = new CiString(toptypeName);
        this.subtypeName = new CiString(subtypeName);
    }

    /**
     * Adds the specified media type parameter. It is a convenience function,
     * the effect is the same as calling:
     * 
     * <pre>
     * parameters.add(new MediaParameter(name, value));
     * </pre>
     * 
     * @return this object, following the builder pattern.
     */
    public MediaType setParameter(MediaParameterKind kind, String value) {
        assert MediaParameterKind.KNOWN_PARAMETERS.contains(kind);
        setParameter(new MediaParameter(kind, value));
        return this;
    }

    public void setParameter(MediaParameter param) {
        ListIterator<MediaParameter> it = parameters.listIterator();
        while (it.hasNext()) {
            MediaParameter p = it.next();
            if (p.kind == Other) {
                if (p.name.equals(param.name)) {
                    it.set(param);
                    return;
                }
            } else {
                if (p.kind == param.kind) {
                    it.set(param);
                    return;
                }
            }
        }
        parameters.add(param);
    }

    public MediaType setParameters(List<MediaParameter> params) {
        for (MediaParameter param : params) {
            setParameter(param);
        }
        return this;
    }


    public Optional<String> parameter(MediaParameterKind kind) {
        assert MediaParameterKind.KNOWN_PARAMETERS.contains(kind);
        for (MediaParameter p : parameters) {
            if (p.kind.equals(kind))
                return Optional.of(p.value);
        }
        return Optional.empty();
    }

    public static MediaType parse(String s) throws ParseException {
        return new StructuredFieldBodyParser().parseContentType(s).mediaType;
    }
    
}
