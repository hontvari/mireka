package mireka.address.parser.base;

import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.Deque;

public abstract class CharParser {
    protected CharScanner scanner;
    protected CharToken currentToken;
    protected Spelling spelling = new Spelling();
    private Deque<Integer> inputPositionStack = new ArrayDeque<Integer>();

    protected CharParser(CharScanner charScanner) {
        this.scanner = charScanner;
        currentToken = charScanner.scan();
    }

    protected CharParser(String source) {
        this(new CharScanner(source));
    }

    protected void accept(char ch) throws ParseException {
        if (currentToken.ch == ch)
            acceptIt();
        else
            throw currentToken.syntaxException(CharUtil.toVisibleChar(ch));
    }

    protected void accept(CharClass charClass) throws ParseException {
        if (charClass.isSatisfiedBy(currentToken.ch))
            acceptIt();
        else
            throw currentToken.syntaxException(charClass);
    }

    protected void acceptIt() {
        spelling.appendChar(currentToken.ch);
        currentToken = scanner.scan();
    }

    protected void acceptThem(int count) {
        for (int i = 0; i < count; i++)
            acceptIt();
    }

    protected String peekString(int length) {
        if (length < 0)
            throw new IllegalArgumentException("Length: " + length);
        if (currentToken.ch == -1 || length == 0) {
            return "";
        } else {
            StringBuilder buffer = new StringBuilder();
            buffer.append((char) currentToken.ch);
            if (length > 1)
                buffer.append(scanner.peekString(length - 1));
            return buffer.toString();
        }
    }

    protected void pushSpelling() {
        spelling.start();
    }

    protected String popSpelling() {
        return spelling.finish();
    }

    protected void pushPosition() {
        inputPositionStack.push(currentToken.position);
    }

    protected int popPosition() {
        return inputPositionStack.pop();
    }
}
