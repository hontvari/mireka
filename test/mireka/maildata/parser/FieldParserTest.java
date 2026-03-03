package mireka.maildata.parser;

import static mireka.maildata.parser.Kind.*;
import static org.junit.Assert.assertEquals;

import java.text.ParseException;

import org.junit.Test;

import mireka.maildata.HeaderField;
import mireka.maildata.HeaderFieldText;
import mireka.maildata.field.AddressListField;
import mireka.maildata.field.UnstructuredField;

public class FieldParserTest {
    private HeaderFieldText src(String s) {
        HeaderFieldText text = new HeaderFieldText();
        text.originalSpelling = s;
        text.unfoldedSpelling = s;
        return text;
    }

    @Test
    public void testSubject() throws ParseException {
        HeaderField field = FieldParser.parse(src("subject: Hello world!"));

        assertEquals(UnstructuredField.class, field.getClass());
        assertEquals("subject", field.name);
        assertEquals(SUBJECT, field.kind);
        assertEquals(" Hello world!", ((UnstructuredField) field).body);
    }

    @Test
    public void testFrom() throws ParseException {
        HeaderField field = FieldParser.parse(src("From: john@example.com"));

        assertEquals(AddressListField.class, field.getClass());
        assertEquals(FROM, field.kind);
    }

}
