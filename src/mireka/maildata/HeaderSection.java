package mireka.maildata;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

/**
 * HeaderSection maintains the list of header fields, both in their raw form as
 * extracted from a mail, and in their structured form after being parsed or
 * newly created.
 */
public class HeaderSection {
    /**
     * The fields either in their parsed or unparsed (
     * {@link UnparsedHeaderField}) form.
     */
    private final List<HeaderField> fields = new ArrayList<>();
    boolean isUpdated;

    void addExtracted(HeaderFieldText text) {
        HeaderField field = new UnparsedHeaderField();
        field.source = text;

        int iColon = text.unfoldedSpelling.indexOf(':');
        if (iColon != -1) {
            field.lowerCaseName = text.unfoldedSpelling.substring(0, iColon)
                    .trim().toLowerCase(Locale.US);
        }
        fields.add(field);
    }

    public void append(HeaderField field) {
        fields.add(field);
        isUpdated = true;
    }

    public void prepend(HeaderField field) {
        fields.add(0, field);
        isUpdated = true;
    }

    public void updateOrAppend(HeaderField newHeader) {
        boolean isAlreadyAdded = false;
        for (ListIterator<HeaderField> it = fields.listIterator(); it.hasNext();) {
            HeaderField header = it.next();
            if (newHeader.lowerCaseName.equals(header.lowerCaseName)) {
                if (isAlreadyAdded) {
                    it.remove();
                } else {
                    it.set(newHeader);
                    isAlreadyAdded = true;
                }
            }
        }
        if (!isAlreadyAdded)
            append(newHeader);
        isUpdated = true;
    }

    public <T> T get(Class<T> fieldClass, String fieldName)
            throws ParseException {
        String lowerCaseFieldName = fieldName.toLowerCase(Locale.US);
        for (int i = 0; i < fields.size(); i++) {
            if (lowerCaseFieldName.equals(fields.get(i).lowerCaseName)) {
                HeaderField field = fields.get(i);
                if (field instanceof UnparsedHeaderField) {
                    field = new FieldParser()
                            .parseField(field.source.unfoldedSpelling);
                    fields.set(i, field);
                }
                return fieldClass.cast(field);
            }
        }
        return null;
    }

    /**
     * Returns an iterator to the fields either in their parsed or unparsed (
     * {@link UnparsedHeaderField}) form.
     */
    Iterator<HeaderField> iterator() {
        return fields.listIterator();
    }
}
