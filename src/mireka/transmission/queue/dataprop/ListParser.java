package mireka.transmission.queue.dataprop;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

class ListParser<T> {
    private final StringToElementConverter<T> elementConverter;
    private final String inputString;
    private final List<T> list = new ArrayList<T>();
    private int i = 0;

    public ListParser(String inputString,
            StringToElementConverter<T> elementConverter) {
        this.inputString = inputString;
        this.elementConverter = elementConverter;
    }

    public List<T> parse() throws ParseException {
        parseInner();
        return list;
    }

    private void parseInner() throws ParseException {
        skipWhitespaces();
        if (peek() == -1)
            return;
        while (true) {
            if (peek() == '"') {
                addQuotedElement();
            } else {
                addUnquotedElement();
                skipWhitespaces();
            }

            int ch = peek();
            if (ch == -1) {
                return;
            } else if (ch == ',') {
                read(); // skip ','
                skipWhitespaces();
                continue;
            } else {
                throw new ParseException("Expected: ',' or EOF, received: '"
                        + ch + "'", i);
            }
        }
    }

    private void skipWhitespaces() throws ParseException {
        while (nextIsWhitespace())
            read();
    }

    private boolean nextIsWhitespace() throws ParseException {
        int ch = peek();
        return ch == ' ' || ch == '\t';
    }

    private int peek() throws ParseException {
        if (i > inputString.length())
            throw new ParseException("EOF already read", i);
        else if (i == inputString.length())
            return -1;
        return inputString.charAt(i);
    }

    private int read() throws ParseException {
        int ch = peek();
        i++;
        return ch;
    }

    private void addQuotedElement() throws ParseException {
        String elementString = readQuotedString();
        addString(elementString);
    }

    private void addString(String elementString) {
        T element = elementConverter.toElement(elementString);
        list.add(element);
    }

    private String readQuotedString() throws ParseException {
        int ch = read();
        if (ch != '"')
            throw new IllegalStateException();
        StringBuilder buffer = new StringBuilder();
        while ('"' != (ch = peek())) {
            if (ch == '\\') {
                buffer.append(readEscapedChar());
            } else if (ch == -1) {
                throw new ParseException("Unexpected EOF", i);
            } else {
                buffer.append((char) read());
            }
        }
        read(); // closing quote
        skipWhitespaces(); // spaces after ending quote
        return buffer.toString();
    }

    private char readEscapedChar() throws ParseException {
        read(); // skip backslash
        int ch = read();
        if (ch == -1)
            throw new ParseException("Unexpected EOF", i);
        return (char) ch;
    }

    private void addUnquotedElement() throws ParseException {
        String elementString = readUnquotedString();
        addString(elementString);
    }

    private String readUnquotedString() throws ParseException {
        StringBuilder buffer = new StringBuilder();
        while (!nextIsEndOfUnquotedString()) {
            buffer.append((char) read());
        }
        String elementString = buffer.toString().trim();
        return elementString;
    }

    private boolean nextIsEndOfUnquotedString() throws ParseException {
        int ch = peek();
        return ch == ',' || ch == -1;
    }

}