package mireka.maildata;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import mireka.ExampleMailData;
import mireka.MailData;
import mireka.maildata.field.UnstructuredField;

import org.junit.Test;

public class WritableMailDataTest {

    @Test
    public void testUpdateByPrependingHeader() throws IOException {
        // write
        MailData simpleMail = ExampleMailData.simple();
        WritableMaildata writableMailData = new WritableMaildata(simpleMail);
        HeaderSection headers = writableMailData.getHeaders();
        UnstructuredField testHeader = new UnstructuredField();
        testHeader.setName("X-Test");
        testHeader.body = " 1";
        headers.prepend(testHeader);
        ByteArrayOutputStream resultBuffer = new ByteArrayOutputStream();
        writableMailData.writeTo(resultBuffer);

        // binary stream check
        ByteArrayOutputStream expectedBuffer = new ByteArrayOutputStream();
        expectedBuffer.write("X-Test: 1\r\n".getBytes("ASCII"));
        expectedBuffer.write(ExampleMailData.simple().bytes);
        assertArrayEquals(expectedBuffer.toByteArray(),
                resultBuffer.toByteArray());
    }

}
