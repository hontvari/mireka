package mireka.smtp.address.parser.base;

import java.util.ArrayDeque;
import java.util.Deque;

public class Spelling {
    private StringBuilder buffer = new StringBuilder();
    private Deque<Integer> startPositionStack = new ArrayDeque<Integer>();

    public void start() {
        startPositionStack.push(buffer.length());
    }

    public String finish() {
        int start = startPositionStack.pop();
        return buffer.substring(start);
    }

    public void append(String s) {
        buffer.append(s);
    }

    public void appendChar(int ch) {
        if (ch != -1)
            buffer.append((char) ch);
    }

}
