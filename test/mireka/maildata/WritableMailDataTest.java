package mireka.maildata;

import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import mireka.ExampleMaildata;
import mireka.ExampleMaildataFile;
import mireka.maildata.field.UnstructuredField;
import mireka.maildata.parser.Kind;

public class WritableMailDataTest {

    @Test
    public void testUpdateByPrependingHeader() throws IOException {
        // write
        Maildata maildata = ExampleMaildata.simple();
        HeaderSection headers = maildata.headers();
        UnstructuredField testHeader = new UnstructuredField(Kind.OTHER);
        testHeader.name = "X-Test";
        testHeader.body = " 1";
        headers.prepend(testHeader);
        ByteArrayOutputStream resultBuffer = new ByteArrayOutputStream();
        maildata.writeTo(resultBuffer);

        // binary stream check
        ByteArrayOutputStream expectedBuffer = new ByteArrayOutputStream();
        expectedBuffer.write("X-Test: 1\r\n".getBytes("ASCII"));
        expectedBuffer.write(ExampleMaildataFile.simple().bytes);
        assertArrayEquals(expectedBuffer.toByteArray(),
                resultBuffer.toByteArray());
    }

}
