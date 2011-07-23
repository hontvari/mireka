package mireka.forward;

import static org.junit.Assert.*;

import org.junit.Test;

public class Base32IntTest {
    @Test
    public void testEncode() throws Exception {
        assertEquals("B7", Base32Int.encode10Bits(63));
    }

    @Test
    public void testDecode() throws Exception {
        assertEquals(63, Base32Int.decode("B7"));

    }
}
