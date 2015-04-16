package mireka.maildata;

import static org.junit.Assert.*;

import java.util.List;

import mockit.Deencapsulation;

import org.junit.Test;

public class EncodedWordGeneratorTest {

    @Test
    public void testSimple() {
        List<String> list =
                new EncodedWordGenerator().generate("Jon",
                        EncodedWordGenerator.Placement.PHRASE);
        assertEquals(list.size(), 1);
        assertEquals("=?UTF-8?Q?Jon?=", list.get(0));
    }

    @Test
    public void testSpace() {
        List<String> list =
                new EncodedWordGenerator().generate("Jon Postel",
                        EncodedWordGenerator.Placement.PHRASE);
        assertEquals("=?UTF-8?Q?Jon_Postel?=", list.get(0));
    }

    @Test
    public void testUnderscore() {
        List<String> list =
                new EncodedWordGenerator().generate("LIKE_CONSTANT",
                        EncodedWordGenerator.Placement.PHRASE);
        assertEquals("=?UTF-8?Q?LIKE=5FCONSTANT?=", list.get(0));
    }

    @Test
    public void testAccented() {
        List<String> list =
                new EncodedWordGenerator().generate("Hontvári",
                        EncodedWordGenerator.Placement.PHRASE);
        assertEquals("=?UTF-8?Q?Hontv=C3=A1ri?=", list.get(0));
    }

    @Test
    public void testAccentedB() {
        List<String> list =
                new EncodedWordGenerator().generate("úüű",
                        EncodedWordGenerator.Placement.PHRASE);
        assertEquals("=?UTF-8?B?w7rDvMWx?=", list.get(0));
    }

    @Test
    public void testAccentedBFiller() {
        List<String> list =
                new EncodedWordGenerator().generate("úü",
                        EncodedWordGenerator.Placement.PHRASE);
        assertEquals("=?UTF-8?B?w7rDvA==?=", list.get(0));
    }

    @Test
    public void testLong() {
        EncodedWordGenerator generator = new EncodedWordGenerator();
        // overhead is 12 octets
        Deencapsulation.setField(generator, "MAX_LENGTH", 24);
        List<String> list =
                generator.generate("This is a long statement",
                        EncodedWordGenerator.Placement.PHRASE);
        assertEquals(2, list.size());
        assertEquals("=?UTF-8?Q?This_is_a_lo?=", list.get(0));
        assertEquals("=?UTF-8?Q?ng_statement?=", list.get(1));
    }

    /**
     * Generator must not split a surrogate pair
     */
    @Test
    public void testSupplementalCharacter() {
        EncodedWordGenerator generator = new EncodedWordGenerator();
        // overhead is 12 octets
        Deencapsulation.setField(generator, "MAX_LENGTH", 24);
        // Example character is U+1F347 GRAPES 🍇
        // UTF-8: 0xF0 0x9F 0x8D 0x87
        // UTF-16: 0xD83C 0xDF47
        List<String> list =
                generator.generate("alfa\uD83C\uDF47beta",
                        EncodedWordGenerator.Placement.PHRASE);
        assertEquals(3, list.size());
        assertEquals("=?UTF-8?Q?alfa?=", list.get(0));
        assertEquals("=?UTF-8?Q?=F0=9F=8D=87?=", list.get(1));
        assertEquals("=?UTF-8?Q?beta?=", list.get(2));
    }
}
