package mireka.sieve;

import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

import mireka.ResourceLoader;

public class InterpreterTest {

    @Test
    public void test() {
        byte[] bytes = ResourceLoader.loadResource(InterpreterTest.class, "extended-example.sieve");
        System.out.println(Arrays.toString(bytes));
        fail("Not yet implemented");
    }

}
