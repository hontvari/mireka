package mireka;

import java.util.Arrays;

import org.mockito.ArgumentMatcher;

public class ArrayEndsWith extends ArgumentMatcher<byte[]> {
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
        byte[] actualEnd = Arrays.copyOfRange(argumentArray, argumentArray.length
                - expectedEnd.length, argumentArray.length);
        return Arrays.equals(expectedEnd, actualEnd);
    }
}
