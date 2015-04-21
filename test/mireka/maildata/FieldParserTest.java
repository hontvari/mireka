package mireka.maildata;

import static org.junit.Assert.*;

import java.text.ParseException;

import org.junit.Test;

public class FieldParserTest {

    @Test
    public void testSubject() throws ParseException {
        HeaderField field = FieldParser.parse("Subject: Hello world!");

        assertEquals(UnstructuredField.class, field.getClass());
        assertEquals("Subject", field.name);
        assertEquals("subject", field.lowerCaseName);
        assertEquals(" Hello world!", ((UnstructuredField) field).body);
    }

    @Test
    public void testFrom() throws ParseException {
        HeaderField field = FieldParser.parse("From: john@example.com");

        assertEquals(FromField.class, field.getClass());
    }

}
