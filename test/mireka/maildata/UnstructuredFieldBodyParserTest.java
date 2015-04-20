package mireka.maildata;

import static org.junit.Assert.*;

import java.text.ParseException;

import org.junit.Test;

public class UnstructuredFieldBodyParserTest {

    @Test
    public void testSimple() throws ParseException {
        UnstructuredHeader header =
                new UnstructuredFieldBodyParser(" Hello world!").parse();

        assertEquals(" Hello world!", header.body);
    }

    @Test
    public void testWithEncodedWord() throws ParseException {
        UnstructuredHeader header =
                new UnstructuredFieldBodyParser(
                        " [LIST] =?US-ASCII?Q?Hello_world!?=").parse();

        assertEquals(" [LIST] Hello world!", header.body);
    }

    @Test
    public void testWithEncodedWordSequence() throws ParseException {
        UnstructuredHeader header =
                new UnstructuredFieldBodyParser(
                        " =?ISO-8859-1?B?SWYgeW91IGNhbiByZWFkIHRoaXMgeW8=?= "
                                + "=?ISO-8859-2?B?dSB1bmRlcnN0YW5kIHRoZSBleGFtcGxlLg==?=")
                        .parse();

        assertEquals(" If you can read this you understand the example.",
                header.body);
    }

    @Test
    public void testWithoutBeginningSpace() throws ParseException {
        UnstructuredHeader header =
                new UnstructuredFieldBodyParser("=?US-ASCII?Q?Hello_world!?=")
                        .parse();

        assertEquals("Hello world!", header.body);
    }

}
