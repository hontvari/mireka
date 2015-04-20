package mireka.maildata;

import static org.junit.Assert.*;

import java.text.ParseException;

import org.junit.Test;

public class FieldParserTest {

    @Test
    public void testSubject() throws ParseException {
        HeaderField field = FieldParser.parse("Subject: Hello world!");

        assertEquals(UnstructuredHeader.class, field.getClass());
        assertEquals("Subject", field.name);
        assertEquals("subject", field.lowerCaseName);
        assertEquals(" Hello world!", ((UnstructuredHeader) field).body);
    }

    @Test
    public void testFrom() throws ParseException {
        HeaderField field = FieldParser.parse("From: john@example.com");

        assertEquals(FromHeader.class, field.getClass());
    }

}
