package mireka.maildata.parser;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UnstructuredFieldBodyParserTest {

    @Test
    public void testSimple() {
        String body = new UnstructuredFieldBodyParser(" Hello world!").parse();

        assertEquals(" Hello world!", body);
    }

    @Test
    public void testWithEncodedWord() {
        String body = new UnstructuredFieldBodyParser(" [LIST] =?US-ASCII?Q?Hello_world!?=")
                .parse();

        assertEquals(" [LIST] Hello world!", body);
    }

    @Test
    public void testWithEncodedWordSequence() {
        String body = new UnstructuredFieldBodyParser(
                " =?ISO-8859-1?B?SWYgeW91IGNhbiByZWFkIHRoaXMgeW8=?= "
                        + "=?ISO-8859-2?B?dSB1bmRlcnN0YW5kIHRoZSBleGFtcGxlLg==?=").parse();

        assertEquals(" If you can read this you understand the example.", body);
    }

    @Test
    public void testWithoutBeginningSpace() {
        String body = new UnstructuredFieldBodyParser("=?US-ASCII?Q?Hello_world!?=").parse();

        assertEquals("Hello world!", body);
    }

}
