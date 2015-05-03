package mireka.maildata;

import static mireka.util.CharsetUtil.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import mireka.maildata.parser.FieldHeaderParser;
import mireka.maildata.parser.FieldHeaderParser.FieldMap;
import mireka.maildata.parser.FieldParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HeaderSection maintains the list of header fields, both in their raw form as
 * extracted from a mail, and in their structured form after being parsed or
 * newly created.
 */
public class HeaderSection {
    private final Logger logger = LoggerFactory.getLogger(HeaderSection.class);
    /**
     * The fields either in their parsed or unparsed (
     * {@link UnparsedHeaderField}) form.
     */
    private final List<Entry> fields = new ArrayList<>();
    boolean isUpdated;

    /**
     * It adds a header field based on the supplied source text to the list.
     * This operation will be called for each header field as they are
     * sequentially extracted from mail data. This operation does not cause the
     * parsing of the field.
     */
    public void addExtracted(HeaderFieldText text) {
        Entry entry = new Entry();
        entry.source = text;

        try {
            FieldMap map = new FieldHeaderParser(text.unfoldedSpelling).parse();
            entry.lowerCaseName = toAsciiLowerCase(map.name);
        } catch (ParseException e) {
            logger.debug(
                    "Cannot parse header field name, preserving header as is",
                    e);
        }
        fields.add(entry);
    }

    public void append(HeaderField field) {
        Entry entry = createEntryForParsedField(field);
        fields.add(entry);
        isUpdated = true;
    }

    public void prepend(HeaderField field) {
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
     * this object by calling {@link #updateOrAppend}, otherwise the change will
     * be lost when the header section is written out.
     */
    public <T extends HeaderField> T get(FieldDef<T> fieldDef)
            throws ParseException {
        String lowerCaseFieldName = toAsciiLowerCase(fieldDef.lowerCaseName());
        for (int i = 0; i < fields.size(); i++) {
            if (lowerCaseFieldName.equals(fields.get(i).lowerCaseName)) {
                HeaderField field = getParsed(i);
                return fieldDef.clazz().cast(field);
            }
        }
        return null;
    }

    /**
     * Returns all occurrences of the specified field in parsed form.
     * 
     * If there are no occurrences of the field then it returns an empty list.
     * 
     * A returned field can be modified, but it has to be reinserted into this
     * object by calling {@link #updateOrAppend}, otherwise the change will be
     * lost when the header section is written out.
     */
    public <T extends HeaderField> List<T> getAll(FieldDef<T> fieldDef)
            throws ParseException {
        String lowerCaseFieldName = toAsciiLowerCase(fieldDef.lowerCaseName());
        List<T> result = new ArrayList<>();
        for (int i = 0; i < fields.size(); i++) {
            if (lowerCaseFieldName.equals(fields.get(i).lowerCaseName)) {
                HeaderField f = getParsed(i);
                T field = fieldDef.clazz().cast(f);
                result.add(field);
            }
        }
        return result;
    }

    /**
     * Returns true if the supplied field presents in the header.
     */
    public boolean contains(FieldDef<?> fieldDef) {
        String lowerCaseFieldName = toAsciiLowerCase(fieldDef.lowerCaseName());
        for (int i = 0; i < fields.size(); i++) {
            if (lowerCaseFieldName.equals(fields.get(i).lowerCaseName)) {
                return true;
            }
        }
        return false;
    }

    public int countOf(FieldDef<?> field) {
        int result = 0;
        for (Entry entry : fields) {
            if (field.lowerCaseName().equals(entry.lowerCaseName))
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
        boolean isAlreadyAdded = false;
        for (ListIterator<Entry> it = fields.listIterator(); it.hasNext();) {
            Entry entry = it.next();
            if (newHeader.lowerCaseName.equals(entry.lowerCaseName)) {
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

    public void remove(FieldDef<?> fieldDef) {
        String lowerCaseFieldName = toAsciiLowerCase(fieldDef.lowerCaseName());
        for (Iterator<Entry> it = fields.iterator(); it.hasNext();) {
            if (lowerCaseFieldName.equals(it.next().lowerCaseName)) {
                it.remove();
                isUpdated = true;
            }
        }
    }

    private Entry createEntryForParsedField(HeaderField field) {
        if (field.lowerCaseName == null)
            throw new NullPointerException();

        Entry entry = new Entry();
        entry.source = null;
        entry.lowerCaseName = field.lowerCaseName;
        entry.parsedField = field;
        return entry;
    }

    /**
     * Returns the parsed version of the specified header field. If the field is
     * currently in unparsed form, then it parses it and stores the parsed
     * field, so the next time the same parsed object will be returned.
     * 
     * @param i
     *            the index of the header within the {@link #fields} list.
     */
    private HeaderField getParsed(int i) throws ParseException {
        Entry entry = fields.get(i);
        if (entry.parsedField == null) {
            entry.parsedField =
                    FieldParser.parse(entry.source.unfoldedSpelling);
            entry.parsedField.source = entry.source;
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

    public static class Entry {
        /**
         * Null if the header is newly created, instead of being extracted from
         * a received mail. It is also null if the body has been updated.
         */
        public HeaderFieldText source;
        public String lowerCaseName;
        /**
         * Null if the field is not yet parsed or if it is unparsable due to
         * syntax errors.
         */
        public HeaderField parsedField;

    }
}
