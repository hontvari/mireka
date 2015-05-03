package mireka.transmission.immediate.dns;

import static mireka.ExampleAddress.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mireka.smtp.SendException;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;

import org.junit.Test;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Name;

public class MxLookupTest {
    @Tested
    private MxLookup mxLookup;

    private final MXRecord HOST1_PRIORITY10 = new MXRecord(EXAMPLE_COM_NAME, 0,
            0, 10, HOST1_EXAMPLE_COM_NAME);
    private final MXRecord HOST2_PRIORITY20 = new MXRecord(EXAMPLE_COM_NAME, 0,
            0, 20, HOST2_EXAMPLE_COM_NAME);
    private final MXRecord HOST2_PRIORITY10 = new MXRecord(EXAMPLE_COM_NAME, 0,
            0, 10, HOST2_EXAMPLE_COM_NAME);
    private final MXRecord HOST3_PRIORITY10 = new MXRecord(EXAMPLE_COM_NAME, 0,
            0, 10, HOST3_EXAMPLE_COM_NAME);
    private final MXRecord HOST4_PRIORITY10 = new MXRecord(EXAMPLE_COM_NAME, 0,
            0, 10, HOST4_EXAMPLE_COM_NAME);

    @Mocked
    private Lookup lookup;

    @Test()
    public void testNoMxRecords() throws MxLookupException {
        new Expectations() {
            {
                lookup.run();
                result = null;

                lookup.getResult();
                result = Lookup.TYPE_NOT_FOUND;
            }

        };

        Name[] targets = mxLookup.queryMxTargets(EXAMPLE_COM_DOMAIN);
        assertArrayEquals(new Name[] { EXAMPLE_COM_NAME }, targets);
    }

    @Test(expected = SendException.class)
    public void testHostNotFound() throws MxLookupException {
        new Expectations() {
            {
                lookup.run();
                result = null;

                lookup.getResult();
                result = Lookup.HOST_NOT_FOUND;
            }

        };

        mxLookup.queryMxTargets(EXAMPLE_COM_DOMAIN);
    }

    @Test()
    public void testDifferentPriority() throws MxLookupException {
        new Expectations() {
            {
                lookup.run();
                result = new MXRecord[] { HOST2_PRIORITY20, HOST1_PRIORITY10 };
            }

        };

        Name[] targets = mxLookup.queryMxTargets(EXAMPLE_COM_DOMAIN);

        assertArrayEquals(new Name[] { HOST1_EXAMPLE_COM_NAME,
                HOST2_EXAMPLE_COM_NAME }, targets);
    }

    @Test()
    public void testSamePriority() throws MxLookupException {
        new Expectations() {
            {
                lookup.run();
                result = new MXRecord[] { HOST1_PRIORITY10, HOST2_PRIORITY10 };
            }

        };

        Name[] result = mxLookup.queryMxTargets(EXAMPLE_COM_DOMAIN);

        Name[] expected =
                new Name[] { HOST1_EXAMPLE_COM_NAME, HOST2_EXAMPLE_COM_NAME };
        assertTrue(sameElements(expected, result));
    }

    @Test()
    public void testSamePriorityReallyShuffled() throws MxLookupException {
        new Expectations() {
            {
                lookup.run();
                result =
                        new MXRecord[] { HOST1_PRIORITY10, HOST2_PRIORITY10,
                                HOST3_PRIORITY10, HOST4_PRIORITY10 };
            }

        };

        final int COUNT_OF_TEST_RUNS = 4;
        List<Name[]> listOfResults = new ArrayList<Name[]>();
        for (int i = 0; i < COUNT_OF_TEST_RUNS; i++) {
            listOfResults.add(mxLookup.queryMxTargets(EXAMPLE_COM_DOMAIN));
        }

        assertTrue(reallyShuffled(listOfResults));
    }

    private static <T> boolean reallyShuffled(List<T[]> listOfResults) {
        T[] firstResult = listOfResults.get(0);
        for (int i = 1; i < listOfResults.size(); i++) {
            T[] result = listOfResults.get(i);
            assertTrue(sameElements(firstResult, result));
            if (!Arrays.equals(firstResult, result))
                return true;
        }
        return false;
    }

    private static <T> boolean sameElements(T[] expected, T[] actual) {
        Set<T> expectedSet = new HashSet<T>(Arrays.asList(expected));
        Set<T> actualSet = new HashSet<T>(Arrays.asList(actual));
        if (expectedSet.size() != expected.length
                || actualSet.size() != actual.length)
            throw new RuntimeException();
        return expectedSet.equals(actualSet);
    }
}
