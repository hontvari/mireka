package mireka.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class MultilineParser {
    private final String text;
    private final List<String> lines = new ArrayList<String>();
    /**
     * Index of the line returned by the next call to {@link #next()}.
     */
    private int index;

    public MultilineParser(String text) {
        this.text = text;
        initializeLines();
    }

    private void initializeLines() {
        try {
            BufferedReader reader = new BufferedReader(new StringReader(text));
            String line;
            while (null != (line = reader.readLine())) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(); // impossible
        }
    }

    public boolean hasNext() {
        return index < lines.size();
    }

    public String next() {
        return lines.get(index++);
    }

    public boolean atFirstLine() {
        return index == 1;
    }

    public boolean atLastLine() {
        return index == lines.size();
    }
}
