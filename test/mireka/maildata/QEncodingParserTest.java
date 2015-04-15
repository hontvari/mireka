package mireka.maildata;

import static org.junit.Assert.*;

import java.text.ParseException;

import org.junit.Test;

public class QEncodingParserTest extends QEncodingParser {

    private String in1 = "hi";
    private byte[] out1 = { 'h', 'i' };

    private String in2 = "h=69_jon";
    private byte[] out2 = { 'h', 'i', ' ', 'j', 'o', 'n' };

    @Test
    public void testSimple() throws ParseException {
        QEncodingParser parser = new QEncodingParser();
        byte[] result = parser.decode(in1);
        assertArrayEquals(out1, result);
    }

    @Test
    public void testComplex() throws ParseException {
        QEncodingParser parser = new QEncodingParser();
        byte[] result = parser.decode(in1);
        assertArrayEquals(out1, result);
    }
}
