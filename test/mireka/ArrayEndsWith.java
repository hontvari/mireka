package mireka;

import java.util.Arrays;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class ArrayEndsWith extends BaseMatcher<byte[]> {
    private final byte[] expectedEnd;

    public ArrayEndsWith(byte[] expectedEnd) {
        super();
        if (expectedEnd == null)
            throw new NullPointerException();
        this.expectedEnd = expectedEnd;
    }

    @Override
    public boolean matches(Object argument) {
        byte[] argumentArray = (byte[]) argument;
        if (argument == null)
            return false;
        if (argumentArray.length < expectedEnd.length)
            return false;
        byte[] actualEnd =
                Arrays.copyOfRange(argumentArray, argumentArray.length
                        - expectedEnd.length, argumentArray.length);
        return Arrays.equals(expectedEnd, actualEnd);
    }

    @Override
    public void describeTo(Description description) {
        if (expectedEnd.length > 10) {
            description
                    .appendText("a byte array which ends with the specified ");
            description.appendText(String.valueOf(expectedEnd.length));
            description.appendText(" bytes");
        } else {
            description.appendValueList("a byte array which ends with bytes ",
                    ",", "", expectedEnd);
        }

    }
}
