package mireka.address.parser;

import static mireka.address.parser.CharClasses.*;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import mireka.address.parser.base.AST;
import mireka.address.parser.base.CharParser;
import mireka.address.parser.base.CharScanner;

public class Ipv4Parser extends CharParser {

    public Ipv4Parser(CharScanner charScanner) {
        super(charScanner);
    }

    public Ipv4 parse() throws ParseException {
        Ipv4 ipv4AST = parseIpv4();
        new AddressContextualAnalyzer(ipv4AST).decorate();
        if (currentToken.ch != -1)
            throw currentToken.otherSyntaxException("Superfluous characters "
                    + "after IPv4 address: {0}.");
        return ipv4AST;
    }

    public Ipv4 parseLeft() throws ParseException {
        Ipv4 ipv4AST = parseIpv4();
        new AddressContextualAnalyzer(ipv4AST).decorate();
        scanner.pushBack(currentToken);
        return ipv4AST;
    }

    private Ipv4 parseIpv4() throws ParseException {
        pushPosition();
        pushSpelling();
        List<Snum> snumASTs = new ArrayList<Snum>();
        snumASTs.add(parseSnum());
        for (int i = 0; i < 3; i++) {
            accept('.');
            snumASTs.add(parseSnum());
        }
        return new Ipv4(popPosition(), popSpelling(), snumASTs);
    }

    private Snum parseSnum() throws ParseException {
        pushPosition();
        pushSpelling();

        accept(DIGIT);
        if (DIGIT.isSatisfiedBy(currentToken.ch))
            accept(DIGIT);
        if (DIGIT.isSatisfiedBy(currentToken.ch))
            accept(DIGIT);
        return new Snum(popPosition(), popSpelling());
    }

    public static class Ipv4 extends AST {
        public String spelling;
        private List<Snum> snums;
        public byte[] addressBytes;
        public InetAddress address;

        public Ipv4(int position, String spelling, List<Snum> snums) {
            super(position);
            this.spelling = spelling;
            this.snums = snums;
        }
    }

    private static class Snum extends AST {
        private String spelling;

        public Snum(int position, String spelling) {
            super(position);
            this.spelling = spelling;
        }

    }

    /**
     * AddressContextualAnalyzer does additional checks on the address which are
     * not included in the grammar and extracts the IPv6 address bytes from the
     * abstract syntax tree.
     */
    private static class AddressContextualAnalyzer {
        final Ipv4 ipv4AST;

        public AddressContextualAnalyzer(Ipv4 ipv4ast) {
            ipv4AST = ipv4ast;
        }

        /**
         * Traverses the abstract tree and decorates the Ipv6 node with the
         * evaluates address in the form of a byte array and an
         * {@link Inet6Address} object.
         */
        public void decorate() throws ParseException {
            byte[] addressBytes = convertToBytes();

            ipv4AST.addressBytes = addressBytes;
            try {
                ipv4AST.address = InetAddress.getByAddress(addressBytes);
            } catch (UnknownHostException e) {
                // this could only happen if the length of the byte array were
                // invalid (not 4), which is impossible.
                throw new RuntimeException("Assertion failed", e);
            }
        }

        private byte[] convertToBytes() throws ParseException {
            byte[] result = new byte[4];
            int pos = 0;
            for (Snum dec : ipv4AST.snums) {
                result[pos++] = evaluateDecByte(dec);
            }
            return result;
        }

        private byte evaluateDecByte(Snum dec) throws ParseException {
            try {
                int result = Integer.parseInt(dec.spelling);
                if (result > 255)
                    throw dec.syntaxException("Byte value must be lower "
                            + "than or equal with 255 in the IPv4 "
                            + "compatible part of an IPv6 address.");
                return (byte) result;
            } catch (NumberFormatException e) {
                throw new RuntimeException("Assertion failed");
            }
        }
    }

}
