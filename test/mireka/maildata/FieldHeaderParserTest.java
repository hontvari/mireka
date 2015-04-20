package mireka.maildata;

import static org.junit.Assert.*;

import java.text.ParseException;

import mireka.maildata.FieldHeaderParser.FieldMap;

import org.junit.Test;

public class FieldHeaderParserTest {

    @Test
    public void testSimple() throws ParseException {
        FieldMap map = new FieldHeaderParser("Subject: Hello world!").parse();

        assertEquals("Subject", map.name);
        assertEquals(8, map.indexOfBody);
    }

    @Test
    public void testObsolete() throws ParseException {
        FieldMap map = new FieldHeaderParser("Subject  : Hello world!").parse();

        assertEquals("Subject", map.name);
        assertEquals(10, map.indexOfBody);
    }
}
