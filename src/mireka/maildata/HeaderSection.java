package mireka.maildata;

import static mireka.maildata.parser.Kind.KNOWN_TYPES;
import static mireka.util.CharsetUtil.toAsciiBytes;

import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mireka.imap.CiString;
import mireka.maildata.field.UnstructuredField;
import mireka.maildata.io.PositionOutputStream;
import mireka.maildata.io.Range;
import mireka.maildata.io.Subsource;
import mireka.maildata.parser.FieldHeaderParser;
import mireka.maildata.parser.FieldHeaderParser.FieldMap;
import mireka.maildata.parser.FieldParser;
import mireka.maildata.parser.Kind;
import mireka.util.CharsetUtil;

/**
 * HeaderSection maintains the list of header fields, both in their raw form as
 * extracted from a mail, and in their structured form after being parsed or
 * newly created.
 */
public class HeaderSection {
    private final Logger logger = LoggerFactory.getLogger(HeaderSection.class);
    /**
     * The fields either in their parsed or unparsed form.
     */
    private final List<Entry> fields = new ArrayList<>();
    /**
     * It is set to true by the update methods to indicate that an update happened
     */
    boolean isUpdated;
    /**
     * The range of this header section within the source. Only valid if not {@link #isUpdated}.
     */
    @Nullable
    public Subsource sourceRange;

    /**
     * It adds a header field based on the supplied source text to the list.
     * This operation will be called for each header field as they are
     * sequentially extracted from mail data. This operation does not cause the
     * parsing of the field.
     */
    public void addExtracted(HeaderFieldText text) {
        logger.trace("addExtractedHeader {}", text.originalSpelling);
        Entry entry = new Entry();
        entry.source = text;

        try {
            FieldMap map = new FieldHeaderParser(text.unfoldedSpelling).parse();
            entry.kind = Kind.forHeaderFieldName(map.name);
            entry.name = map.name;
        } catch (ParseException e) {
            logger.debug(
                    "Cannot parse header field name, preserving header as is",
                    e);
            entry.kind = Kind.BADLY_FORMATTED;
        }
        fields.add(entry);
    }

    /**
     * Appends a newly generated field
     */
    public void append(HeaderField field) {
        assert KNOWN_TYPES.contains(field.kind);
        assert field.name != null;

        Entry entry = createEntryForParsedField(field);
        fields.add(entry);
        isUpdated = true;
    }

    /**
     * Prepends a newly generated field
     */
    public void prepend(HeaderField field) {
        assert KNOWN_TYPES.contains(field.kind);
        assert field.name != null;

        Entry entry = createEntryForParsedField(field);
        fields.add(0, entry);
        isUpdated = true;
    }

    /**
     * Returns the specified field in parsed form.
     * 
     * If there are more fields in the header with the same name, then it
     * returns the first one.
     * 
     * The returned object can be modified, but it has to be reinserted into
     * this object by calling {@link #put}, otherwise the change will
     * be lost when the header section is written out.
     */
    public HeaderField get(Kind kind)
            throws ParseException {
        assert KNOWN_TYPES.contains(kind);
        for (Entry entry : fields) {
            if (entry.kind == kind) {
                return getParsed(entry);
            }
        }
        return null;
    }

    /**
     * Same as {@link #get(Kind)} but the returned type is specified in an argument.
     */
    public <T> T get(Kind kind, Class<T> clazz) throws ParseException {
        return clazz.cast(get(kind));
    }

    /**
     * Returns all occurrences of the specified field in parsed form.
     * 
     * If there are no occurrences of the field then it returns an empty list.
     * 
     * A returned field can be modified, but it has to be reinserted into this
     * object by calling {@link #remove} and e.g. {@link #append}, otherwise the change will be
     * lost when the header section is written out.
     */
    public List<HeaderField> getAll(Kind kind)
            throws ParseException {
        assert KNOWN_TYPES.contains(kind);
        List<HeaderField> result = new ArrayList<>();
        for (Entry entry : fields) {
            if (entry.kind == kind) {
                result.add(getParsed(entry));
            }
        }
        return result;
    }

    /**
     * Same as {@link #getAll(Kind)} but the returned element type is specified in an argument.
     */
    public <T extends HeaderField> List<T> getAll(Kind kind, Class<T> clazz) throws ParseException {
        List<T> r = new ArrayList<>();
        for (HeaderField f : getAll(kind)) {
            r.add(clazz.cast(f));
        }
        return r;
    }

    /**
     * Returns all header fields with the supplied name, as a parsed unstructured field. This
     * function is useful for generic query functionality, like the header test in the sieve script
     * language.
     * 
     * @see #getAll(Kind)
     */
    public List<HeaderField> getAll(CiString name) throws ParseException {
        List<HeaderField> result = new ArrayList<>();
        for (Entry entry : fields) {
            if (name.equals(entry.name)) {
                result.add(getParsed(entry));
            }
        }
        return result;
    }

    /**
     * Returns true if the supplied field presents in the header.
     */
    public boolean contains(Kind kind) {
        assert KNOWN_TYPES.contains(kind);
        for (Entry entry : fields) {
            if (entry.kind == kind) {
                return true;
            }
        }
        return false;
    }

    public int countOf(Kind kind) {
        assert KNOWN_TYPES.contains(kind);
        int result = 0;
        for (Entry entry : fields) {
            if (entry.kind == kind)
                result++;
        }
        return result;
    }

    /**
     * Adds the header to the end of the header section if there is no header
     * with the same name, or replaces the first occurrence of the header and
     * removes all others.
     */
    public void put(HeaderField newHeader) {
        assert KNOWN_TYPES.contains(newHeader.kind);
        assert newHeader.name != null;

        boolean isAlreadyAdded = false;
        for (ListIterator<Entry> it = fields.listIterator(); it.hasNext();) {
            Entry entry = it.next();
            if (newHeader.kind == entry.kind) {
                if (isAlreadyAdded) {
                    it.remove();
                } else {
                    Entry newEntry = createEntryForParsedField(newHeader);
                    it.set(newEntry);
                    isAlreadyAdded = true;
                }
            }
        }
        if (!isAlreadyAdded)
            append(newHeader);
        isUpdated = true;
    }

    public void remove(Kind kind) {
        assert KNOWN_TYPES.contains(kind);
        for (Iterator<Entry> it = fields.iterator(); it.hasNext();) {
            if (it.next().kind == kind) {
                it.remove();
                isUpdated = true;
            }
        }
    }

    /**
     * Creates a new Entry object for a generated header field
     */
    private Entry createEntryForParsedField(HeaderField field) {
        assert KNOWN_TYPES.contains(field.kind);
        assert field.name != null;

        Entry entry = new Entry();
        entry.kind = field.kind;
        entry.source = null;
        entry.parsedField = field;
        return entry;
    }

    /**
     * Returns the parsed version of the specified header field. If the field is
     * currently in unparsed form, then it parses it and stores the parsed
     * field, so the next time the same parsed object will be returned.
     */
    private HeaderField getParsed(Entry entry) throws ParseException {
        if (entry.parsedField == null) {
            entry.parsedField = FieldParser.parse(entry.source);
        }
        return entry.parsedField;
    }

    /**
     * Returns an iterator to the fields either in their parsed or unparsed (
     * {@link UnparsedHeaderField}) form.
     */
    Iterator<Entry> entries() {
        return fields.listIterator();
    }

    void writeTo(OutputStream out) throws IOException {
        for (Entry entry : fields) {
            if (entry.source == null) {
                String fieldAsString = entry.parsedField.generate();
                out.write(CharsetUtil.toAsciiBytes(fieldAsString));
            } else {
                out.write(toAsciiBytes(entry.source.originalSpelling));
            }
        }
    }

    public void save(PositionOutputStream out) throws IOException {
        long start = out.position;
        if (isUpdated) {
            for (Entry entry : fields) {
                entry.save(out);
            }
            isUpdated = false;
        } else {
            out.copy(sourceRange);
        }
        sourceRange = out.subsource(start);
    }

    public Range range() {
        if (isUpdated)
            throw new IllegalStateException();
        // TODO Auto-generated method stub
        return null;
    }

    public static class Entry {
        public Kind kind;
        /**
         * Header field name. It may be null if the field is really badly formatted, in that case
         * {@link #kind} is {@link Kind#BADLY_FORMATTED}.
         */
        public CiString name;
        /**
         * Null if the header is newly created, instead of being extracted from
         * a received mail. It is also null if the body has been updated.
         */
        public HeaderFieldText source;
        /**
         * Null if the field is not yet parsed or if it is unparsable due to
         * syntax errors.
         */
        public HeaderField parsedField;
        /**
         * The field parsed as an unstructured field, in contrast to {@link #parsedField}, which
         * contains the field content in a structured form, if the field structure is known for this
         * module.
         * 
         * For fields which are known to be unstructured, this value is the same as
         * {@link #parsedField}.
         * 
         * Null if the field is not parsed, at least not parsed as an unstructured field.
         */
        public UnstructuredField unstructuredField;

        public void save(OutputStream out) throws IOException {
            if (source == null) {
                source = parsedField.generate();
            }
            out.write(toAsciiBytes(source.originalSpelling));
        }
    }
}
