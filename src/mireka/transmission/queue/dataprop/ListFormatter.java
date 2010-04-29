package mireka.transmission.queue.dataprop;

import java.util.List;

class ListFormatter {
    public final StringBuilder buffer = new StringBuilder();
    private final List<?> list;

    public ListFormatter(List<?> list) {
        this.list = list;
    }

    public String format() {
        formatInner();
        return buffer.toString();
    }

    private void formatInner() {
        for (Object element : list) {
            if (buffer.length() != 0)
                buffer.append(", ");
            buffer.append(escapeListElementIfNecessary(element.toString()));
        }
    }

    private String escapeListElementIfNecessary(String element) {
        if (!containsSpecialListCharacters(element))
            return element;
        StringBuilder buffer = new StringBuilder();
        buffer.append('"');
        for (int i = 0; i < element.length(); i++) {
            char ch = element.charAt(i);
            if (ch == '"')
                buffer.append("\\\"");
            else if (ch == '\\')
                buffer.append("\\\\");
            else
                buffer.append(ch);
        }
        buffer.append('"');
        return buffer.toString();
    }

    private boolean containsSpecialListCharacters(String element) {
        for (int i = 0; i < element.length(); i++) {
            char ch = element.charAt(i);
            if (ch == '"' || ch == ',' || ch == '\\')
                return true;
        }
        return false;
    }

}