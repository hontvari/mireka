package mireka.maildata.io;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import mockit.Deencapsulation;

import org.junit.Test;

public class DeferredFileTest {

    @Test
    public final void testMemory() throws IOException {
        DeferredFile file = new DeferredFile();
        file.transitionSize = 5;

        OutputStream out = file.getOutputStream();
        out.write('Z');
        out.close();

        assertNull(Deencapsulation.getField(file, File.class));
        InputStream in = file.getInputStream();
        assertEquals('Z', in.read());
        assertEquals(-1, in.read());
        in.close();
        file.close();
    }

    @Test
    public final void testDisk() throws IOException {
        DeferredFile file = new DeferredFile();
        file.transitionSize = 5;

        OutputStream out = file.getOutputStream();
        out.write('Z');
        out.write('s');
        out.write('u');
        out.write('z');
        out.write('s');
        out.write('o');
        out.close();

        File f = Deencapsulation.getField(file, File.class);
        assertNotNull(f);
        assertTrue(f.exists());

        InputStream in = file.getInputStream();
        assertEquals('Z', in.read());
        assertEquals('s', in.read());
        assertEquals('u', in.read());
        assertEquals('z', in.read());
        assertEquals('s', in.read());
        assertEquals('o', in.read());
        assertEquals(-1, in.read());
        in.close();

        file.close();
        assertFalse(f.exists());
    }
}
