package mireka.maildata;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

/**
 * EncodedWordGenerator generates one or more encoded-word strings from a
 * non-ASCII character input.
 * 
 * An 'encoded-word' is used to put a non-ASCII character string into some part
 * of the mail data headers, for example into the display-name part of the From
 * header.
 * 
 * Encoded-words have a maximum length, therefore it may be necessary to
 * generate several words to encode a long text.
 * 
 * <pre>
 * encoded-word = "=?" charset "?" encoding "?" encoded-text "?="
 * </pre>
 * 
 * RFC 2047 allows any character encoding, but this class always uses UTF-8.
 * 
 * @see <a href="https://tools.ietf.org/html/rfc2047">RFC 2047: MIME
 *      (Multipurpose Internet Mail Extensions) Part Three: Message Header
 *      Extensions for Non-ASCII Text</a>
 */
public class EncodedWordGenerator {
    private static final String EOF = "";
    /**
     * Maximum length of a single encoded-word nonterminal.
     */
    private int MAX_LENGTH = 75;
    private Placement placement;
    private Encoder encoder;
    private Charset charset = Charset.forName("UTF-8");
    private int overhead;
    private Scanner scanner;
    private String currentCodepointString;

    public List<String> generate(String text, Placement placement) {
        this.placement = placement;
        String encoding;
        if (isMostlyAscii(text)) {
            encoder = new QEncoder();
            encoding = "Q";
        } else {
            encoder = new BEncoder();
            encoding = "B";
        }
        String header = "=?UTF-8?" + encoding + "?";
        overhead = header.length() + "?=".length();
        scanner = new Scanner(text);
        currentCodepointString = scanner.scan();
        List<String> result = new ArrayList<>();

        while (currentCodepointString != EOF) {
            String encodedText = encoder.generate();
            String encodedWord = header + encodedText + "?=";
            result.add(encodedWord);
        }

        return result;
    }

    private boolean isMostlyAscii(String text) {
        int cAscii = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) <= 127)
                cAscii++;
        }
        return (double) cAscii / text.length() > 0.4;
    }

    private void acceptIt() {
        currentCodepointString = scanner.scan();
    }

    /**
     * The placement of an encoded-word determines the set of allowed characters
     * which may appear in the Q encoded-text nonterminal.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc2047#section-5">Use of
     *      encoded-words in message headers</a>
     */
    public static enum Placement {
        TEXT {

            @Override
            boolean isAllowed(int c) {
                return isEncodedTextChar(c);
            }

        },

        COMMENT {

            @Override
            boolean isAllowed(int c) {
                if (!isEncodedTextChar(c))
                    return false;

                switch (c) {
                case '(':
                case ')':
                case '\\':
                    return false;
                default:
                    return true;
                }
            }
        },

        PHRASE {

            @Override
            boolean isAllowed(int c) {
                if (isUpperCaseLetter(c) || isLowerCaseLetter(c) || isDigit(c))
                    return true;

                switch (c) {
                case '!':
                case '*':
                case '+':
                case '-':
                case '/':
                case '=':
                case '_':
                    return true;
                default:
                    return false;
                }
            }

            private boolean isUpperCaseLetter(int c) {
                return 'A' <= c && c <= 'Z';
            }

            private boolean isLowerCaseLetter(int c) {
                return 'a' <= c && c <= 'z';
            }

            private boolean isDigit(int c) {
                return '0' <= c && c <= '9';
            }
        };

        abstract boolean isAllowed(int c);

        /**
         * Returns true if the supplied character matches the placement
         * independent rule: Any printable ASCII character other than "?" or
         * SPACE.
         */
        private static boolean isEncodedTextChar(int c) {
            if (c < 33 || c > 126)
                return false;
            if (c == '?')
                return false;
            return true;
        }
    }

    /**
     * Encoder implementations (Q and B) generate the encoded-text nonterminal.
     */
    private interface Encoder {
        /**
         * Generates a single encoded-text nonterminal, consuming as many input
         * tokens as it can without exceeding the maximum allowed length.
         */
        String generate();
    }

    /**
     * QEncoder implements the quoted-printable like Q encoding algorithm.
     */
    private class QEncoder implements Encoder {
        private StringBuilder buffer;

        @Override
        public String generate() {
            buffer = new StringBuilder(80);

            while (currentCodepointString != EOF) {
                byte[] currentBytes = currentCodepointString.getBytes(charset);
                String encoded = encode(currentBytes);
                if (buffer.length() + encoded.length() + overhead <= MAX_LENGTH) {
                    buffer.append(encoded);
                    acceptIt();
                } else {
                    break;
                }
            }
            return buffer.toString();
        }

        private String encode(byte[] bytes) {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                int b = bytes[i] & 0xFF;
                if (b == 0x20) {
                    result.append('_');
                } else if (placement.isAllowed(b) && !specialMeaningInQ(b)) {
                    result.append((char) b);
                } else {
                    result.append('=');
                    result.append(toHexDigit((b & 0xF0) >> 4));
                    result.append(toHexDigit(b & 0x0F));
                }
            }
            return result.toString();
        }

        /**
         * Returns true for those bytes which must always be hex encoded because
         * they have special meanings in Q encoding.
         */
        private boolean specialMeaningInQ(int b) {
            switch (b) {
            case '=':
            case '?':
            case '_':
                return true;
            default:
                return false;
            }
        }

        char toHexDigit(int v) {
            return (char) (v < 10 ? '0' + v : 'A' + v - 10);
        }
    }

    /**
     * BEncoder implements the BASE64 like B encoding algorithm.
     */
    private class BEncoder implements Encoder {
        private ByteArrayOutputStream buffer;

        @Override
        public String generate() {
            try {
                buffer = new ByteArrayOutputStream(80);

                while (currentCodepointString != EOF) {
                    byte[] currentBytes =
                            currentCodepointString.getBytes(charset);
                    if (doesItFit(currentBytes.length)) {
                        buffer.write(currentBytes);
                        acceptIt();
                    } else {
                        break;
                    }
                }
                String result =
                        DatatypeConverter.printBase64Binary(buffer
                                .toByteArray());
                return result;
            } catch (IOException e) {
                throw new RuntimeException("Assertion failed", e);
            }
        }

        /**
         * Returns true if the supplied number of bytes can still be added to
         * the buffer before the length of the entire word
         * 
         * @param length
         * @return
         */
        private boolean doesItFit(int additionalBytes) {
            int totalBytes = buffer.size() + additionalBytes;
            // byteCount * 4/3 rounded up
            int encodedSize = (totalBytes + 2) / 3 * 4;
            return encodedSize + overhead <= MAX_LENGTH;
        }
    }

    /**
     * Scanner splits the input string into codepoints, assuring that no
     * surrogate pairs are split into two separate encoded words.
     */
    private class Scanner {
        private int currentChar;
        private Reader in;

        public Scanner(String text) {
            this.in = new StringReader(text);
            try {
                currentChar = in.read();
            } catch (IOException e) {
                throw new RuntimeException("Assertion failed", e);
            }
        }

        /**
         * Returns a string corresponding to the next codepoint. Specifically it
         * returns a
         * <ul>
         * <li>1 character string if the next character is a BMP character
         * <li>2 characters, a surrogate pair if the next two character
         * represents a supplementary character
         * <li>an empty string, specifically the EOF String at the end of the
         * input.
         * </ul>
         */
        public String scan() {
            if (currentChar == -1) {
                return EOF;
            } else if (Character.isHighSurrogate((char) currentChar)) {
                StringBuilder result = new StringBuilder();
                result.append((char) currentChar);
                takeIt();
                result.append((char) currentChar);
                takeIt();
                return result.toString();
            } else {
                String result = Character.toString((char) currentChar);
                takeIt();
                return result;
            }

        }

        private void takeIt() {
            try {
                currentChar = in.read();
            } catch (IOException e) {
                throw new RuntimeException("Assertion failed", e);
            }
        }
    }
}
