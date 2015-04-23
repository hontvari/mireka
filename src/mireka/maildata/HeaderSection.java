package mireka.maildata;

import static mireka.util.CharsetUtil.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import mireka.maildata.FieldHeaderParser.FieldMap;

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
    void addExtracted(HeaderFieldText text) {
        Entry entry = new Entry();
        entry.source = text;

        try {
            FieldMap map = new FieldHeaderParser(text.unfoldedSpelling).parse();
            // entry.name = map.name;
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

    private Entry createEntryForParsedField(HeaderField field) {
        if (field.lowerCaseName == null)
            throw new NullPointerException();

        Entry entry = new Entry();
        entry.source = null;
        entry.lowerCaseName = field.lowerCaseName;
        entry.parsedField = field;
        return entry;
    }

    public void prepend(HeaderField field) {
        Entry entry = createEntryForParsedField(field);
        fields.add(0, entry);
        isUpdated = true;
    }

    public void updateOrAppend(HeaderField newHeader) {
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

    /**
     * Returns the specified field in parsed form.
     * 
     * The returned object can be modified, but it has to be reinserted into
     * this object by calling {@link #updateOrAppend}, otherwise the change will
     * be lost when the header section is written out.
     */
    public <T> T get(Class<T> fieldClass, String fieldName)
            throws ParseException {
        String lowerCaseFieldName = toAsciiLowerCase(fieldName);
        for (int i = 0; i < fields.size(); i++) {
            if (lowerCaseFieldName.equals(fields.get(i).lowerCaseName)) {
                HeaderField field = getParsed(i);
                return fieldClass.cast(field);
            }
        }
        return null;
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
