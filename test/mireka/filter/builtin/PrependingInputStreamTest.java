package mireka.filter.builtin;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import mireka.filter.builtin.spf.PrependingInputStream;

import org.junit.Test;

public class PrependingInputStreamTest {
    private byte[] header = new byte[] { 1, 2, 3 };
    private byte[] body = new byte[] { 4, 5, 6 };
    private byte[] expected = new byte[] { 1, 2, 3, 4, 5, 6 };
    private InputStream bodyStream = new ByteArrayInputStream(body);

    @Test
    public void testSingleReads() throws IOException {
        PrependingInputStream stream =
                new PrependingInputStream(header, bodyStream);
        byte[] actual = readByBytes(stream);
        assertArrayEquals(expected, actual);
    }

    private byte[] readByBytes(PrependingInputStream stream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int byteRead;
        while (-1 != (byteRead = stream.read()))
            buffer.write(byteRead);
        byte[] actual = buffer.toByteArray();
        return actual;
    }

    @Test
    public void testBlockRead() throws IOException {
        PrependingInputStream stream =
                new PrependingInputStream(header, bodyStream);
        byte[] buffer = new byte[100];
        int cRead = stream.read(buffer);
        
        assertTrue(cRead != -1);
        byte[] actual = Arrays.copyOf(buffer, cRead);
        assertArrayEquals(expected, actual);
    }
}
