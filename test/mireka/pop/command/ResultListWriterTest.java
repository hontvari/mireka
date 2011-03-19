package mireka.pop.command;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;

import org.junit.Test;

public class ResultListWriterTest {

    @Test
    public final void testWriteLine() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ResultListWriter resultListWriter = new ResultListWriter(outputStream);
        resultListWriter.writeLine("a");
        resultListWriter.writeLine("b");
        resultListWriter.endList();
        assertArrayEquals("a\r\nb\r\n.\r\n".getBytes("US-ASCII"),
                outputStream.toByteArray());
    }

}
