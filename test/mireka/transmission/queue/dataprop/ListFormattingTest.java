package mireka.transmission.queue.dataprop;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class ListFormattingTest {
    private final static ToStringConverter TO_STRING_CONVERTER =
            new ToStringConverter();

    @Test
    public void testFormatNoSpecialCharacters() throws ParseException {
        List<String> sourceList =
                new ArrayList<String>(Arrays.asList("John", "Jane"));
        String s = new ListFormatter(sourceList).format();

        List<String> parsedList =
                new ListParser<String>(s, TO_STRING_CONVERTER).parse();
        assertEquals(sourceList, parsedList);
    }

    @Test
    public void testFormatSpecialCharacters() throws ParseException {
        String specialString = "jo\"hn";
        List<String> sourceList = Collections.singletonList(specialString);
        String s = new ListFormatter(sourceList).format();

        List<String> parsedList =
                new ListParser<String>(s, TO_STRING_CONVERTER).parse();
        assertEquals(sourceList, parsedList);
    }

    private final static class ToStringConverter implements
            StringToElementConverter<String> {
        @Override
        public String toElement(String s) {
            return s;
        }
    }

}
