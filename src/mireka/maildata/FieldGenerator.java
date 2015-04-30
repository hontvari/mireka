package mireka.maildata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import mireka.maildata.EncodedWordGenerator.Placement;
import mireka.maildata.field.AddressListField;
import mireka.maildata.field.ContentType;
import mireka.maildata.field.MimeVersion;
import mireka.maildata.field.UnstructuredField;
import mireka.util.CharsetUtil;

public class FieldGenerator {
    private Folder folder = new Folder();

    public String writeUnstructuredHeader(UnstructuredField field) {
        if (field.body == null)
            throw new NullPointerException();

        folder.t(field.name).t(":");
        writeUnstructuredBody(field.body);
        return folder.toString();

    }

    private void writeUnstructuredBody(String text) {
        if (isAscii(text)
                && !new Scanner(text)
                        .testUnstructuredBodyContainsWordLikeEncodedWord())
            writeUnstructuredUtext(text);
        else
            writeEncodedWords(text, Placement.TEXT);
    }

    private void writeUnstructuredUtext(String text) {
        CharScanner scanner = new CharScanner(text);
        int currentChar = scanner.scan();
        folder.begin();

        while (currentChar != CharScanner.EOF) {
            if (currentChar == ' ' || currentChar == '\t') {
                folder.fsp((char) currentChar);
                currentChar = scanner.scan();
            } else {
                folder.t((char) currentChar);
                currentChar = scanner.scan();
            }
        }

        folder.end();
    }

    private void writeMailboxList(List<Mailbox> mailboxList) {
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
        folder.begin();
        folder.t('"');

        while (currentChar != CharScanner.EOF) {
            if (currentChar == '"' || currentChar == '\\') {
                folder.t('\\').t((char) currentChar);
                currentChar = scanner.scan();
            } else if (currentChar == ' ' || currentChar == '\t') {
                folder.fsp((char) currentChar);
                currentChar = scanner.scan();
            } else {
                folder.t((char) currentChar);
                currentChar = scanner.scan();
            }
        }
        folder.t('"');
        folder.end();
    }

    private void writeDomainLiteral(String literalAddress) {
        CharScanner scanner = new CharScanner(literalAddress);
        int currentChar = scanner.scan();
        folder.begin();
        folder.t('[');

        while (currentChar != CharScanner.EOF) {
            if (currentChar == '[' || currentChar == ']' || currentChar == '\\') {
                folder.t("\\").t((char) currentChar);
                currentChar = scanner.scan();
            } else if (currentChar == ' ' || currentChar == '\t') {
                folder.fsp((char) currentChar);
                currentChar = scanner.scan();
            } else {
                folder.t((char) currentChar);
                currentChar = scanner.scan();
            }
        }

        folder.t(']');
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
        folder.begin();

        while (currentChar != CharScanner.EOF) {
            if (currentChar == ' ') {
                folder.fsp(" ");
                currentChar = scanner.scan();
            } else {
                folder.t((char) currentChar);
                currentChar = scanner.scan();
            }
        }

        folder.end();
    }

    private boolean isAtomPhrase(String phrase) {
        return new Scanner(phrase).testAtomPhrase();
    }

    private boolean isDotAtom(String s) {
        return new Scanner(s).testDotAtom();
    }

    public String writeAddressListField(AddressListField field) {
        if (field.addressList.isEmpty())
            throw new IllegalStateException("addressList is empty");

        folder.t(field.name).t(':').fsp(' ');
        writeAddressList(field.addressList);
        return folder.toString();
    }

    private void writeAddressList(List<Address> addressList) {
        if (addressList.isEmpty())
            return;

        writeAddress(addressList.get(0));
        for (int i = 1; i < addressList.size(); i++) {
            folder.t(",");
            folder.fsp(" ");
            writeAddress(addressList.get(i));
        }
    }

    private void writeAddress(Address address) {
        if (address instanceof Mailbox) {
            writeMailbox((Mailbox) address);
        } else if (address instanceof Group) {
            writeGroup((Group) address);
        } else {
            throw new RuntimeException("Assertion failed");
        }
    }

    private void writeGroup(Group group) {
        folder.begin();
        writePhrase(group.displayName);
        folder.t(':').fsp(' ');
        writeGroupList(group.mailboxList);
        folder.t(';');
        folder.end();
    }

    /**
     * <pre>
     * group           =   display-name ":" [group-list] ";" [CFWS]
     * group-list      =   mailbox-list / CFWS / obs-group-list
     * </pre>
     */
    private void writeGroupList(List<Mailbox> mailboxList) {
        if (mailboxList.isEmpty()) {
            // a CFWS has been already written after the group ':' character for
            // formatting purposes, so nothing to do here
        } else {
            writeMailboxList(mailboxList);
        }
    }

    public String writeMimeVersion(MimeVersion mimeVersion) {
        throw new RuntimeException("Not implemented");
    }

    public String writeContentType(ContentType contentType) {
        throw new RuntimeException("Not implemented");
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

        public boolean testUnstructuredBodyContainsWordLikeEncodedWord() {
            while (isLWSP() || isUtext()) {
                if (isLWSP()) {
                    scanLWSP();
                } else if (isUtext()) {
                    String word = scanUtextWord();
                    if (isLikeEncodedWord(word))
                        return true;
                } else {
                    throw new RuntimeException("Assertion failed");
                }
            }
            take(isEOF());
            return false;
        }

        private String scanUtextWord() {
            StringBuilder result = new StringBuilder();
            result.append((char) currentChar);
            take(isUtext());
            while (isUtext()) {
                result.append((char) currentChar);
                takeIt();
            }
            return result.toString();
        }

        /**
         * Every ASCII character except LWSP
         */
        private boolean isUtext() {
            if (currentChar < 0 || currentChar > 127)
                return false;

            switch (currentChar) {
            case ' ':
            case '\t':
                return false;
            default:
                return true;
            }
        }

        private void scanLWSP() {
            take(isLWSP());
            while (isLWSP()) {
                takeIt();
            }
        }

        private boolean isLWSP() {
            return currentChar == ' ' || currentChar == '\t';
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

    private static class CharScanner {
        private final static int EOF = -1;
        private int currentChar;
        private ByteArrayInputStream in;

        public CharScanner(String source) {
            this.in =
                    new ByteArrayInputStream(CharsetUtil.toAsciiBytes(source));
            currentChar = in.read();
        }

        public int scan() {
            int result = currentChar;
            takeIt();
            return result;
        }

        private void takeIt() {
            currentChar = in.read();
        }
    }
}
