package mireka.address.parser.base;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CharScanner {
    private final String input;
    private int index = 0;

    private int currentChar;
    private int currentSpelling;

    private LinkedList<CharToken> pushBackBuffer = new LinkedList<CharToken>();

    public CharScanner(String input) {
        this.input = input;

        // store first char
        if (input.isEmpty())
            currentChar = -1;
        else
            currentChar = input.charAt(0);
    }

    public CharToken scan() {
        if (!pushBackBuffer.isEmpty()) {
            CharToken result = pushBackBuffer.removeFirst();
            return result;
        }

        int position = index;
        takeIt();
        CharToken result = new CharToken(currentSpelling, position);
        return result;
    }

    public String peekString(int length) {
        StringBuilder buffer = new StringBuilder();
        List<CharToken> charTokens = new ArrayList<CharToken>();
        for (int i = 0; i < length; i++) {
            CharToken charToken = scan();
            charTokens.add(charToken);
            if (charToken.ch == -1)
                break;
            else
                buffer.append((char) charToken.ch);
        }
        pushBack(charTokens);
        return buffer.toString();
    }

    private void takeIt() {
        currentSpelling = currentChar;
        index++;
        if (index < input.length())
            currentChar = input.charAt(index);
        else
            currentChar = -1;
    }

    public void pushBack(Token token) {
        pushBack(token.getSpellingTokens());
    }

    private void pushBack(List<CharToken> charTokens) {
        pushBackBuffer.addAll(0, charTokens);
    }
}
