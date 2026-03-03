package mireka.maildata.parser;

import java.util.ArrayDeque;
import java.util.Deque;

class SpellingBuffer {

    private final Deque<StringBuilder> stack = new ArrayDeque<>();

    public void begin() {
        stack.push(new StringBuilder());
    }

    public String end() {
        return stack.pop().toString();
    }

    public void append(char ch) {
        for (StringBuilder buffer : stack) {
            buffer.append(ch);
        }
    }

    public void append(String whitespace, String spelling) {
        for (StringBuilder buffer : stack) {
            if (buffer.length() >= 1)
                buffer.append(whitespace);
            buffer.append(spelling);
        }
    }

    public String current() {
        return stack.peek().toString();
    }

    @Override
    public String toString() {
        return stack.toString();
    }

}
