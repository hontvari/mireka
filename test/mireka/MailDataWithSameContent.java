package mireka;

import java.io.IOException;
import java.io.InputStream;

import mireka.maildata.MaildataFile;

import org.mockito.ArgumentMatcher;

public class MailDataWithSameContent extends ArgumentMatcher<MaildataFile> {
    private final MaildataFile other;

    public MailDataWithSameContent(MaildataFile other) {
        super();
        this.other = other;
    }

    @Override
    public boolean matches(Object argument) {
        try {
            InputStream argumentStream = ((MaildataFile) argument).getInputStream();
            InputStream otherStream = other.getInputStream();
            int argumentChar;
            int expectedChar;
            while (true) {
                argumentChar = argumentStream.read();
                expectedChar = otherStream.read();
                if (argumentChar != expectedChar)
                    return false;
                if (expectedChar == -1)
                    return true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
