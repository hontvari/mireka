package mireka.dmarc;

import static org.junit.Assert.*;

import java.text.ParseException;

import org.junit.Test;

public class TagValueListParserTest {

    @Test
    public void testParse() throws ParseException {
        TagValueList tags =
                new TagValueListParser()
                        .parse("v=DMARC1; p=none; rua=mailto:dmarc-feedback@example.com");
        assertEquals(3, tags.list.size());
        assertEquals(3, tags.map.size());

        TagSpec tag = tags.list.get(0);
        assertEquals("v", tag.name);
        assertEquals("DMARC1", tag.value);

        tag = tags.list.get(1);
        assertEquals("p", tag.name);
        assertEquals("none", tag.value);

        tag = tags.list.get(2);
        assertEquals("rua", tag.name);
        assertEquals("mailto:dmarc-feedback@example.com", tag.value);
    }

    @Test
    public void testParseEndingSemicolon() throws ParseException {
        TagValueList tags = new TagValueListParser().parse("v=DMARC1; ");
        assertEquals(1, tags.list.size());

        TagSpec tag = tags.list.get(0);
        assertEquals("v", tag.name);
        assertEquals("DMARC1", tag.value);
    }

}
