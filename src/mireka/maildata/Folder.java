package mireka.maildata;

import java.util.ArrayList;
import java.util.List;

import mireka.util.CharsetUtil;

/**
 * Folder is able to fold a long header field. If possible it folds at higher
 * level constructs, e.g. if the field body is comma-separated values then it
 * tries to fold only after commas, not within the values, even if folding is
 * allowed within values.
 * 
 * Terms:
 * <ul>
 * <li>atom - sequence of characters which must not be wrapped.
 * <li>folding-white-space - typically one linear-white-space character. A line
 * can be wrapped before a folding-white-space.
 * <li>construct - a sequence of atom and folding-space tokens and lower level
 * constructs which together form a semantically significant logical unit.
 * </ul>
 * 
 * This class follows the builder pattern. The content must be added by
 * consecutive calls to the t, fsp, begin and end operations, and when the
 * content is complete the toString or toBytes operation must be called to
 * return the folded string.
 * 
 * @see <a href="https://tools.ietf.org/html/rfc5322#section-2.2.3">RFC5322 Long
 *      Header Fields</a>
 */
public class Folder {
    private List<Token> list = new ArrayList<>();
    private int currentLevel = 1;
    private int softLimit = 78;
    private int hardLimit = 998;

    /**
     * Changes the recommended and never exceed line length (without the ending
     * CRLF. It is useful for testing.
     */
    Folder setLimit(int softLimit, int hardLimit) {
        this.softLimit = softLimit;
        this.hardLimit = hardLimit;
        return this;
    }

    /**
     * Adds an atom.
     * 
     * If the previous token is also an atom then the supplied string is
     * concatenated to the previous atom. The concatenation happens by appending
     * to a StringBuilder object, so it does not cause any excessive memory
     * usage, even for extremely long strings.
     */
    public Folder t(String s) {
        if (s == null)
            throw new NullPointerException();

        Token lastToken = list.isEmpty() ? null : list.get(list.size() - 1);
        if (lastToken instanceof Atom) {
            lastToken.text.append(s);
        } else {
            list.add(new Atom(s));
        }
        return this;
    }

    /**
     * Adds a folding-white-space atom.
     * 
     * @param text
     * @return
     */
    public Folder fsp(String text) {
        list.add(new Fws(text, currentLevel));
        return this;
    }

    /**
     * Marks the beginning of a logical construct. It must be paired with an
     * end() call, but logical constructs can be nested.
     */
    public Folder begin() {
        currentLevel++;
        return this;
    }

    /**
     * Marks the end of the logical construct.
     * 
     * @return
     */
    public Folder end() {
        currentLevel--;
        if (currentLevel < 1)
            throw new IllegalStateException();
        return this;
    }

    /**
     * Returns the input in folded form as a String.
     */
    @Override
    public String toString() {
        return new Printer().print();
    }

    /**
     * Returns the input in folded form as US-ASCII bytes.
     */
    public byte[] toBytes() {
        return CharsetUtil.toAsciiBytes(toString());
    }

    private abstract class Token {
        public StringBuilder text = new StringBuilder();

        public int length() {
            return text.length();
        }
    }

    private class Atom extends Token {

        public Atom(String text) {
            this.text.append(text);
        }
    }

    private class Fws extends Token {
        public Fws(String text, int level) {
            this.text.append(text);
            this.level = level;
        }

        /**
         * Semantic level of the token. Highest level is 1.
         */
        int level;
    }

    private class Printer {
        private StringBuilder buffer = new StringBuilder(256);
        /**
         * Column position within the current line.
         */
        private int column = 0;
        private boolean lineIsOnlyWsp = true;
        private int currentPosition = 0;
        private Token currentToken;

        public String print() {
            currentToken = list.isEmpty() ? null : list.get(0);

            while (currentToken != null) {
                if (currentToken instanceof Atom) {
                    printToken();
                } else if (currentToken instanceof Fws) {
                    if (isWithinSoftLimit(lengthToNextSameOrHigherLevelFws())) {
                        printToken();
                    } else {
                        if (lineIsOnlyWsp || remainingTokensAreAllWsp()) {
                            printToken();
                        } else {
                            fold();
                        }
                    }
                } else {
                    throw new RuntimeException();
                }
            }

            buffer.append("\r\n");
            return buffer.toString();

        }

        private void fold() {
            if (!(currentToken instanceof Fws))
                throw new RuntimeException();

            buffer.append("\r\n");
            buffer.append(currentToken.text);
            column = currentToken.length();
            lineIsOnlyWsp = true;

            takeIt();
        }

        private boolean remainingTokensAreAllWsp() {
            for (int i = currentPosition + 1; i < list.size(); i++) {
                Token token = list.get(i);
                if (token instanceof Atom && !onlyWsp(token.text))
                    return false;
            }
            return true;
        }

        private void takeIt() {
            if (currentPosition >= list.size())
                throw new IllegalStateException();

            currentPosition++;
            currentToken =
                    currentPosition < list.size() ? list.get(currentPosition)
                            : null;
        }

        private boolean isWithinSoftLimit(int length) {
            return column + length <= softLimit;
        }

        private int lengthToNextSameOrHigherLevelFws() {
            int level = ((Fws) currentToken).level;
            int total = currentToken.length();
            int i = currentPosition + 1;

            Token token = i < list.size() ? list.get(i) : null;
            while (token instanceof Atom
                    || (token instanceof Fws && ((Fws) token).level > level)) {
                total += token.length();
                token = ++i < list.size() ? list.get(i) : null;
            }
            return total;
        }

        private void printToken() {
            StringBuilder text = currentToken.text;
            buffer.append(text);
            column += text.length();
            if (lineIsOnlyWsp)
                lineIsOnlyWsp = onlyWsp(text);

            if (column >= hardLimit)
                throw new RuntimeException("Line is too long. '"
                        + buffer.toString() + "'");

            takeIt();
        }

        private boolean onlyWsp(CharSequence s) {
            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                if (ch == ' ' || ch == '\t')
                    continue;
                else
                    return false;
            }
            return true;
        }
    }

}
