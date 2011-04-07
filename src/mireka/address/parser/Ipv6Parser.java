package mireka.address.parser;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import mireka.address.parser.Ipv6Token.Kind;
import mireka.address.parser.base.AST;
import mireka.address.parser.base.CharScanner;
import mireka.address.parser.base.Spelling;
import mireka.address.parser.base.Terminal;

/**
 * Ipv6Parser parses an IPv6 address literal and convert it to an
 * {@link Inet6Address}. For example:
 * <ul>
 * <li>2001:db8:0:0:0:0:0:0
 * <li>2001:db8::
 * <li>2001:db8::1
 * <li>::1
 * <li>2001:db8::192.0.2.0
 * </ul>
 * <p>
 * Grammar:
 * 
 * <pre>
 * IPv6         := [NUM_SEQENCE] ENDING
 * NUM_SEQENCE  := NUM *(: NUM)
 * NUM          := 1*4HEXDIG
 * ENDING       := :: [NUM_SEQENCE [. IPv4Rest]]
 *               | . IPv4Rest
 *               | E
 * IPv4Rest     := NUM . NUM . NUM
 * </pre>
 */
public class Ipv6Parser {
    private Ipv6Scanner scanner;
    private Ipv6Token currentToken;
    private Spelling spelling = new Spelling();

    public Ipv6Parser(String address) throws ParseException {
        this(new CharScanner(address));
    }

    public Ipv6Parser(CharScanner charScanner) throws ParseException {
        this.scanner = new Ipv6Scanner(charScanner);
        currentToken = scanner.scan();
    }

    public Ipv6 parseLeft() throws ParseException {
        Ipv6 ipv6AST = parseIpv6();
        new AddressContextualAnalyzer(ipv6AST).decorate();
        scanner.finish(currentToken);
        return ipv6AST;
    }

    public Ipv6 parse() throws ParseException {
        Ipv6 ipv6AST = parseIpv6();
        new AddressContextualAnalyzer(ipv6AST).decorate();
        if (currentToken.kind != Kind.EOF)
            throw currentToken.otherSyntaxException("Superfluous characters "
                    + "after IPv6 address: {0}.");
        return ipv6AST;
    }

    private Ipv6 parseIpv6() throws ParseException {
        int position = currentToken.position;
        spelling.start();
        NumSequence numSequenceAST = null;
        if (currentToken.kind == Kind.NUM) {
            numSequenceAST = parseNumSequence();
        }
        Ending ending = parseEnding();
        return new Ipv6(position, spelling.finish(), numSequenceAST, ending);
    }

    private NumSequence parseNumSequence() throws ParseException {
        int position = currentToken.position;
        List<Num> numbers = new ArrayList<Num>();
        numbers.add(parseNum());
        while (currentToken.kind == Kind.COLON) {
            acceptIt();
            numbers.add(parseNum());
        }
        return new NumSequence(position, numbers);
    }

    private Num parseNum() throws ParseException {
        if (currentToken.kind == Kind.NUM) {
            Num numAST = new Num(currentToken);
            acceptIt();
            return numAST;
        } else {
            throw currentToken.syntaxException("hex digits");
        }
    }

    private Ending parseEnding() throws ParseException {
        int position = currentToken.position;
        RestOfIpv4 restOfIpv4AST = null;
        switch (currentToken.kind) {
        case DOUBLE_COLON:
            acceptIt();
            NumSequence numSequenceAST = null;

            if (currentToken.kind == Kind.NUM) {
                numSequenceAST = parseNumSequence();
                if (currentToken.kind == Kind.DOT) {
                    acceptIt();
                    restOfIpv4AST = parseRestOfIpv4();
                }
            }
            return new DoubleColonEnding(position, numSequenceAST,
                    restOfIpv4AST);
        case DOT:
            acceptIt();
            restOfIpv4AST = parseRestOfIpv4();
            return new Ipv4Ending(position, restOfIpv4AST);
        case OTHER:
        case EOF:
            return new EmptyEnding(position);
        default:
            throw currentToken
                    .syntaxException("'.', '::' or end of IPv6 literal");
        }
    }

    private RestOfIpv4 parseRestOfIpv4() throws ParseException {
        int position = currentToken.position;
        Num dec2AST = parseNum();
        accept(Kind.DOT);
        Num dec3AST = parseNum();
        accept(Kind.DOT);
        Num dec4AST = parseNum();
        return new RestOfIpv4(position, dec2AST, dec3AST, dec4AST);
    }

    private void accept(Kind kind) throws ParseException {
        if (currentToken.kind == kind)
            acceptIt();
        else
            throw currentToken.syntaxException(kind);
    }

    private void acceptIt() throws ParseException {
        spelling.append(currentToken.spelling);
        currentToken = scanner.scan();
    }

    /**
     * AddressContextualAnalyzer does additional checks on the address which are
     * not included in the grammar and extracts the IPv6 address bytes from the
     * abstract syntax tree.
     */
    private static class AddressContextualAnalyzer {
        final Ipv6 ipv6AST;
        LinkedList<Num> leftNumbers = new LinkedList<Num>();
        LinkedList<Num> rightNumbers = new LinkedList<Num>();
        LinkedList<Num> ipv4Numbers = new LinkedList<Num>();
        boolean hasDoubleColon = false;

        public AddressContextualAnalyzer(Ipv6 ipv6ast) {
            super();
            ipv6AST = ipv6ast;
        }

        /**
         * Traverses the abstract tree and decorates the Ipv6 node with the
         * evaluates address in the form of a byte array and an
         * {@link Inet6Address} object.
         */
        public void decorate() throws ParseException {
            collectNumbers(ipv6AST);
            checkLength();
            byte[] addressBytes = convertToBytes();

            ipv6AST.addressBytes = addressBytes;
            try {
                ipv6AST.address = InetAddress.getByAddress(addressBytes);
            } catch (UnknownHostException e) {
                // this could only happen if the length of the byte array were
                // invalid (not 16), which is impossible.
                throw new RuntimeException("Assertion failed", e);
            }
        }

        private void collectNumbers(Ipv6 ipv6AST) throws ParseException {
            if (ipv6AST.leftNumSequence != null)
                leftNumbers.addAll(ipv6AST.leftNumSequence.numbers);
            if (ipv6AST.ending instanceof DoubleColonEnding) {
                DoubleColonEnding ending = (DoubleColonEnding) ipv6AST.ending;
                hasDoubleColon = true;
                if (ending.rightNumSequence != null) {
                    rightNumbers.addAll(ending.rightNumSequence.numbers);
                    if (ending.restOfIpv4 != null) {
                        ipv4Numbers.add(rightNumbers.removeLast());
                        ipv4Numbers.add(ending.restOfIpv4.dec2);
                        ipv4Numbers.add(ending.restOfIpv4.dec3);
                        ipv4Numbers.add(ending.restOfIpv4.dec4);
                    }
                }
            } else if (ipv6AST.ending instanceof Ipv4Ending) {
                Ipv4Ending ending = (Ipv4Ending) ipv6AST.ending;
                if (ipv6AST.leftNumSequence == null)
                    throw ending.syntaxException("Dot must follow a decimal "
                            + "number in the IPv4 part of an IPv6 address.");
                ipv4Numbers.add(leftNumbers.removeLast());
                ipv4Numbers.add(ending.restOfIpv4.dec2);
                ipv4Numbers.add(ending.restOfIpv4.dec3);
                ipv4Numbers.add(ending.restOfIpv4.dec4);
            } else if (ipv6AST.ending instanceof EmptyEnding) {
                // nothing to do
            }
        }

        private void checkLength() throws ParseException {
            // bytes without the :: placeholder
            int countOfExplicitlySpecifiedBytes =
                    leftNumbers.size() * 2 + rightNumbers.size() * 2
                            + ipv4Numbers.size();
            int maxExplicitlySpecifiedBytes = hasDoubleColon ? 14 : 16;
            if (countOfExplicitlySpecifiedBytes > maxExplicitlySpecifiedBytes)
                throw ipv6AST.syntaxException("IPv6 address literal specifies "
                        + "more than 16 bytes.");
            if (!hasDoubleColon && countOfExplicitlySpecifiedBytes < 16)
                throw ipv6AST.syntaxException("IPv6 address literal specifies "
                        + "less than 16 bytes.");
        }

        private byte[] convertToBytes() throws ParseException {
            byte[] result = new byte[16];
            int pos = 0;
            for (Num hex : leftNumbers) {
                int doubleByte = evaluateHexDoubleByte(hex);
                result[pos++] = (byte) (doubleByte >> 8);
                result[pos++] = (byte) (doubleByte & 0xFF);
            }
            pos = 15;
            for (Iterator<Num> it = ipv4Numbers.descendingIterator(); it
                    .hasNext();) {
                Num dec = it.next();
                result[pos--] = evaluateDecByte(dec);
            }
            for (Iterator<Num> it = rightNumbers.descendingIterator(); it
                    .hasNext();) {
                Num hex = it.next();
                int doubleByte = evaluateHexDoubleByte(hex);
                result[pos--] = (byte) (doubleByte & 0xFF);
                result[pos--] = (byte) (doubleByte >> 8);
            }
            return result;
        }

        private int evaluateHexDoubleByte(Num hex) {
            return Integer.parseInt(hex.spelling, 16);
        }

        private byte evaluateDecByte(Num dec) throws ParseException {
            try {
                int result = Integer.parseInt(dec.spelling);
                if (result > 255)
                    throw dec.syntaxException("Byte value must be lower "
                            + "than or equal with 255 in the IPv4 "
                            + "compatible part of an IPv6 address.");
                return (byte) result;
            } catch (NumberFormatException e) {
                throw dec.syntaxException("The IPv4 compatible part of "
                        + "an IPv6 address must consists of decimal "
                        + "and not hex digits.");
            }
        }
    }

    public static class Ipv6 extends AST {
        public String spelling;
        private NumSequence leftNumSequence;
        private Ending ending;
        public byte[] addressBytes;
        public InetAddress address;

        private Ipv6(int position, String spelling,
                NumSequence leftNumSequence, Ending ending) {
            super(position);
            this.spelling = spelling;
            this.leftNumSequence = leftNumSequence;
            this.ending = ending;
        }

    }

    private static class NumSequence extends AST {

        public List<Num> numbers;

        public NumSequence(int position, List<Num> numbers) {
            super(position);
            this.numbers = numbers;
        }
    }

    private static class Num extends Terminal {

        public Num(Ipv6Token token) {
            super(token.position, token.spelling);
        }

    }

    private static class Ending extends AST {

        public Ending(int position) {
            super(position);
        }

    }

    private static class DoubleColonEnding extends Ending {
        public NumSequence rightNumSequence;
        public RestOfIpv4 restOfIpv4;

        public DoubleColonEnding(int position, NumSequence rightNumSequence,
                RestOfIpv4 restOfIpv4) {
            super(position);
            this.rightNumSequence = rightNumSequence;
            this.restOfIpv4 = restOfIpv4;
        }

    }

    private static class RestOfIpv4 extends AST {
        public Num dec2;
        public Num dec3;
        public Num dec4;

        public RestOfIpv4(int position, Num dec2, Num dec3, Num dec4) {
            super(position);
            this.dec2 = dec2;
            this.dec3 = dec3;
            this.dec4 = dec4;
        }
    }

    public static class Ipv4Ending extends Ending {
        public RestOfIpv4 restOfIpv4;

        public Ipv4Ending(int position, RestOfIpv4 restOfIpv4) {
            super(position);
            this.restOfIpv4 = restOfIpv4;
        }

    }

    public static class EmptyEnding extends Ending {

        public EmptyEnding(int position) {
            super(position);
        }

    }
}
