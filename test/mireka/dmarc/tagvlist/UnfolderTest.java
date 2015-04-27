package mireka.dmarc.tagvlist;

import static org.junit.Assert.*;

import java.text.ParseException;

import mireka.dmarc.tagvlist.Unfolder;

import org.junit.Test;

public class UnfolderTest {

    @Test
    public void test() throws ParseException {
        String result = new Unfolder().unfold("a\r\n b");
        assertEquals("a b", result);
    }

}
