package mireka.maildata.io;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Test;

import mireka.Deencapsulation;

public class DeferredFileTest {

    DeferredFile file = new DeferredFile();

    @Test
    public final void testMemory() throws IOException {
        file.transitionSize = 5;

        OutputStream out = file.getOutputStream();
        out.write('Z');
        out.close();

        assertNull(Deencapsulation.getField(file, "outFile"));
        InputStream in = file.getInputStream();
        assertEquals('Z', in.read());
        assertEquals(-1, in.read());
        in.close();
        file.close();
    }

    @Test
    public final void testDisk() throws IOException {
        file.transitionSize = 5;

        OutputStream out = file.getOutputStream();
        out.write('Z');
        out.write('s');
        out.write('u');
        out.write('z');
        out.write('s');
        out.write('o');
        out.close();

        File outFile = (File) Deencapsulation.getField(file, "outFile");
        assertNotNull(outFile);
        assertTrue(outFile.exists());

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
        assertFalse(outFile.exists());
    }
}
