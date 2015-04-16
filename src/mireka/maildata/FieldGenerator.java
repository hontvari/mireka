package mireka.maildata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import mireka.util.CharsetUtil;

public class FieldGenerator {
    private Folder folder = new Folder();

    public String writeUnstructuredHeader(UnstructuredHeader header) {
        if (header.body == null)
            throw new NullPointerException();

        folder.t(header.name).t(":");
        writeUnstructured(header.body);
        return folder.toString();

    }

    private void writeUnstructured(String text) {
        CharScanner scanner = new CharScanner(text);
        int currentChar = scanner.scan();
        StringBuilder buffer = new StringBuilder();
        folder.begin();

        while (currentChar != CharScanner.EOF) {
            if (currentChar == ' ' || currentChar == '\t') {
                if (buffer.length() != 0) {
                    folder.t(buffer.toString());
                    buffer.setLength(0);
                }
                folder.fsp(Character.toString((char) currentChar));
                currentChar = scanner.scan();
            } else {
                buffer.append((char) currentChar);
                currentChar = scanner.scan();
            }
        }
        if (buffer.length() != 0) {
            folder.t(buffer.toString());
        }

        folder.end();
    }

    public String writeFromHeader(FromHeader header) {
        if (header.mailboxList.isEmpty())
            throw new IllegalStateException("mailboxList is empty");

        folder.t("From:").fsp(" ");
        writeMailboxList(header.mailboxList);
        return folder.toString();
    }

    private void writeMailboxList(List<Mailbox> mailboxList) {
        if (mailboxList.isEmpty())
            return;

        writeMailbox(mailboxList.get(0));
        for (int i = 1; i < mailboxList.size(); i++) {
            folder.t(",");
            folder.fsp(" ");
            writeMailbox(mailboxList.get(i));
        }
    }

    private void writeMailbox(Mailbox mailbox) {
        folder.begin();
        if (mailbox.displayName == null) {
            writeAddrSpec(mailbox.addrSpec);
        } else {
            writeDisplayName(mailbox.displayName);
            folder.fsp(" ");
            folder.t("<");
            writeAddrSpec(mailbox.addrSpec);
            folder.t(">");
        }
        folder.end();
    }

    private void writeAddrSpec(AddrSpec addrSpec) {
        folder.begin();
        writeLocalPart(addrSpec.localPart);
        folder.t("@");
        writeDomain(addrSpec.domain);
        folder.end();
    }

    private void writeLocalPart(String localPart) {
        if (isDotAtom(localPart)) {
            folder.t(localPart);
        } else {
            writeQuotedString(localPart);
        }
    }

    private void writeDomain(DomainPart domainPart) {
        if (domainPart instanceof DotAtomDomainPart) {
            folder.t(((DotAtomDomainPart) domainPart).domain);
        } else if (domainPart instanceof LiteralDomainPart) {
            writeDomainLiteral(((LiteralDomainPart) domainPart).literal);
        } else {
            throw new RuntimeException("Unexpected exception");
        }
    }

    private void writeQuotedString(String text) {
        CharScanner scanner = new CharScanner(text);
        int currentChar = scanner.scan();
        StringBuilder buffer = new StringBuilder();
        folder.begin();
        buffer.append('"');

        while (currentChar != CharScanner.EOF) {
            if (currentChar == '"' || currentChar == '\\') {
                buffer.append('\\').append(currentChar);
                currentChar = scanner.scan();
            } else if (currentChar == ' ' || currentChar == '\t') {
                if (buffer.length() != 0) {
                    folder.t(buffer.toString());
                    buffer.setLength(0);
                }
                folder.fsp(Character.toString((char) currentChar));
                currentChar = scanner.scan();
            } else {
                buffer.append((char) currentChar);
                currentChar = scanner.scan();
            }
        }
        buffer.append('"');
        folder.t(buffer.toString());
        folder.end();
    }

    private void writeDomainLiteral(String literalAddress) {
        CharScanner scanner = new CharScanner(literalAddress);
        int currentChar = scanner.scan();
        StringBuilder buffer = new StringBuilder();
        folder.begin();
        buffer.append('[');

        while (currentChar != CharScanner.EOF) {
            if (currentChar == '[' || currentChar == ']' || currentChar == '\\') {
                buffer.append('\\').append(currentChar);
                currentChar = scanner.scan();
            } else if (currentChar == ' ' || currentChar == '\t') {
                if (buffer.length() != 0) {
                    folder.t(buffer.toString());
                    buffer.setLength(0);
                }
                folder.fsp(Character.toString((char) currentChar));
                currentChar = scanner.scan();
            } else {
                buffer.append((char) currentChar);
                currentChar = scanner.scan();
            }
        }
        if (buffer.length() != 0) {
            folder.t(buffer.toString());
        }

        buffer.append(']');
        folder.end();
    }

    private void writeDisplayName(String displayName) {
        writePhrase(displayName);
    }

    private void writePhrase(String phrase) {
        if (!isAscii(phrase)) {
            writeEncodedWords(phrase, EncodedWordGenerator.Placement.PHRASE);
        } else if (isAtomPhrase(phrase) && !containsWordLikeEncodedWord(phrase)) {
            writeAtomPhrase(phrase);
        } else {
            writeQuotedString(phrase);
        }
    }

    private boolean containsWordLikeEncodedWord(String phrase) {
        return new Scanner(phrase).testAtomPhraseContainsWordLikeEncodedWord();
    }

    private boolean isAscii(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) > 127)
                return false;
        }
        return true;
    }

    private void writeEncodedWords(String text,
            EncodedWordGenerator.Placement placement) {
        List<String> words =
                new EncodedWordGenerator().generate(text, placement);
        if (words.size() > 1)
            folder.begin();
        folder.t(words.get(0));
        for (int i = 1; i < words.size(); i++) {
            folder.fsp(" ");
            folder.t(words.get(i));
        }
        if (words.size() > 1)
            folder.end();
    }

    private void writeAtomPhrase(String phrase) {
        CharScanner scanner = new CharScanner(phrase);
        int currentChar = scanner.scan();
        StringBuilder buffer = new StringBuilder();
        folder.begin();

        while (currentChar != CharScanner.EOF) {
            if (currentChar == ' ') {
                if (buffer.length() != 0) {
                    folder.t(buffer.toString());
                    buffer.setLength(0);
                }
                folder.fsp(" ");
                currentChar = scanner.scan();
            } else {
                buffer.append((char) currentChar);
                currentChar = scanner.scan();
            }
        }
        if (buffer.length() != 0) {
            folder.t(buffer.toString());
        }

        folder.end();
    }

    private boolean isAtomPhrase(String phrase) {
        return new Scanner(phrase).testAtomPhrase();
    }

    private boolean isDotAtom(String s) {
        return new Scanner(s).testDotAtom();
    }

    private class Scanner {
        private final static int EOF = -1;
        private int currentChar;
        private InputStream in;

        private Scanner(String source) {
            this.in =
                    new ByteArrayInputStream(CharsetUtil.toAsciiBytes(source));
            try {
                currentChar = in.read();
            } catch (IOException e) {
                throw new RuntimeException("Unexpected exception", e);
            }
        }

        public boolean testDotAtom() {
            if (!take(isAtext()))
                return false;
            while (isAtext()) {
                takeIt();
            }

            while (currentChar == '.') {
                takeIt();
                if (!take(isAtext()))
                    return false;
                while (isAtext()) {
                    takeIt();
                }
            }

            return take(isEOF());
        }

        /**
         * Returns true if the input is a single space separated atom list. This
         * means that it can be directly written into mail data without quoting.
         */
        public boolean testAtomPhrase() {
            if (!take(isAtext()))
                return false;
            while (isAtext()) {
                takeIt();
            }
            while (currentChar == ' ') {
                takeIt();
                if (!take(isAtext()))
                    return false;
                while (isAtext()) {
                    takeIt();
                }
            }
            return take(isEOF());
        }

        /**
         * Tests if an phrase which consists of only space separated atoms
         * contains a word which is similar to an encoded word.
         */
        public boolean testAtomPhraseContainsWordLikeEncodedWord() {
            StringBuilder atom = new StringBuilder();

            atom.append((char) currentChar);
            take(isAtext());
            while (isAtext()) {
                atom.append((char) currentChar);
                takeIt();
            }
            if (isLikeEncodedWord(atom.toString()))
                return true;

            while (!isEOF()) {
                take(isSpace());
                atom.setLength(0);

                atom.append((char) currentChar);
                take(isAtext());
                while (isAtext()) {
                    atom.append((char) currentChar);
                    takeIt();
                }
                if (isLikeEncodedWord(atom.toString()))
                    return true;
            }

            return false;
        }

        private boolean isLikeEncodedWord(String word) {
            return word.startsWith("=?") && word.endsWith("?=");
        }

        private boolean isSpace() {
            return currentChar == ' ';
        }

        private boolean isAtext() {
            if (isAlpha() || isDigit())
                return true;
            switch (currentChar) {
            case '!':
            case '#':
            case '$':
            case '%':
            case '&':
            case '\'':
            case '*':
            case '+':
            case '-':
            case '/':
            case '=':
            case '?':
            case '^':
            case '_':
            case '`':
            case '{':
            case '|':
            case '}':
            case '~':
                return true;
            }
            return false;
        }

        private boolean isAlpha() {
            if (0x41 <= currentChar && currentChar <= 0x5A)
                return true;
            if (0x61 <= currentChar && currentChar <= 0x7A)
                return true;
            return false;
        }

        private boolean isDigit() {
            return 0x30 <= currentChar && currentChar <= 0x39;
        }

        private boolean isEOF() {
            return currentChar == EOF;
        }

        private void takeIt() {
            try {
                currentChar = in.read();
            } catch (IOException e) {
                throw new RuntimeException("Unexpected exception", e);
            }
        }

        private boolean take(boolean predicate) {
            if (predicate) {
                takeIt();
                return true;
            } else {
                return false;
            }
        }
    }

    private class CharScanner {
        private final static int EOF = -1;
        private int currentChar;
        private InputStream in;

        public CharScanner(String source) {
            this.in =
                    new ByteArrayInputStream(CharsetUtil.toAsciiBytes(source));
            try {
                currentChar = in.read();
            } catch (IOException e) {
                throw new RuntimeException("Unexpected exception", e);
            }
        }

        public int scan() {
            int result = currentChar;
            takeIt();
            return result;
        }

        private void takeIt() {
            try {
                currentChar = in.read();
            } catch (IOException e) {
                throw new RuntimeException("Unexpected exception", e);
            }
        }
    }

}
