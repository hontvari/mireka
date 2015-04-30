package mireka.maildata;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import mireka.ExampleMailData;
import mireka.maildata.field.UnstructuredField;

import org.junit.Test;

public class WritableMailDataTest {

    @Test
    public void testUpdateByPrependingHeader() throws IOException {
        // write
        MaildataFile simpleMail = ExampleMailData.simple();
        Maildata writableMailData = new Maildata(simpleMail);
        HeaderSection headers = writableMailData.getHeaders();
        UnstructuredField testHeader = new UnstructuredField();
        testHeader.setName("X-Test");
        testHeader.body = " 1";
        headers.prepend(testHeader);
        ByteArrayOutputStream resultBuffer = new ByteArrayOutputStream();
        MaildataFile resultMaildata = writableMailData.toMailData();
        resultMaildata.writeTo(resultBuffer);

        // binary stream check
        ByteArrayOutputStream expectedBuffer = new ByteArrayOutputStream();
        expectedBuffer.write("X-Test: 1\r\n".getBytes("ASCII"));
        expectedBuffer.write(ExampleMailData.simple().bytes);
        assertArrayEquals(expectedBuffer.toByteArray(),
                resultBuffer.toByteArray());
    }

}
