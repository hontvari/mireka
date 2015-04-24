package mireka.maildata;

import static org.junit.Assert.*;

import java.text.ParseException;

import org.junit.Test;

public class EncodedWordParserTest {

    @Test
    public void testBEncoding() throws ParseException {
        String result =
                new EncodedWordParser()
                        .parse("=?ISO-8859-1?B?SWYgeW91IGNhbiByZWFkIHRoaXMgeW8=?=");
        assertEquals("If you can read this yo", result);
    }

    @Test
    public void testQEncoding() throws ParseException {
        String result =
                new EncodedWordParser()
                        .parse("=?ISO-8859-1?Q?Keld_J=F8rn_Simonsen?=");
        assertEquals("Keld Jørn Simonsen", result);
    }

    @Test
    public void testLanguageTag() throws ParseException {
        String result =
                new EncodedWordParser()
                        .parse("=?ISO-8859-1*NO?Q?Keld_J=F8rn_Simonsen?=");
        assertEquals("Keld Jørn Simonsen", result);
    }

}
