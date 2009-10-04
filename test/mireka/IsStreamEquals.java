package mireka;

import java.io.IOException;
import java.io.InputStream;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class IsStreamEquals extends BaseMatcher<InputStream> {
    private final InputStream expected;

    public IsStreamEquals(InputStream expected) {
        super();
        this.expected = expected;
    }

    @Override
    public boolean matches(Object item) {
        try {
            InputStream actual = (InputStream) item;
            int argumentChar;
            int expectedChar;
            while (true) {
                argumentChar = actual.read();
                expectedChar = expected.read();
                if (argumentChar != expectedChar)
                    return false;
                if (expectedChar == -1)
                    return true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("Is stream equals");
    }

}
