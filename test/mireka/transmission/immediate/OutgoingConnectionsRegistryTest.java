package mireka.transmission.immediate;

import static mireka.ExampleAddress.IP1;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import mireka.Deencapsulation;
import mireka.transmission.immediate.host.OutgoingConnectionsRegistry;

public class OutgoingConnectionsRegistryTest {
    private OutgoingConnectionsRegistry registry =
            new OutgoingConnectionsRegistry();

    @Test
    public void testOpenClose() throws PostponeException {
        registry.setMaxConnectionsToHost(1);
        registry.openConnection(IP1);
        registry.releaseConnection(IP1);
        assertTrue(Deencapsulation.getField(registry, "connections", Map.class).isEmpty());
    }

    @Test(expected = PostponeException.class)
    public void test1() throws PostponeException {
        registry.setMaxConnectionsToHost(1);
        registry.openConnection(IP1);
        registry.openConnection(IP1);
    }

    @Test(expected = RuntimeException.class)
    public void test1Unbalanced() {
        registry.releaseConnection(IP1);
    }

    @Test()
    public void testSwitchedOff() throws PostponeException {
        registry.setMaxConnectionsToHost(0);
        registry.releaseConnection(IP1);
        registry.openConnection(IP1);
    }

    @Test()
    public void test2() throws PostponeException {
        registry.setMaxConnectionsToHost(2);
        registry.openConnection(IP1);
        registry.openConnection(IP1);
        registry.releaseConnection(IP1);
        registry.releaseConnection(IP1);
        assertTrue(Deencapsulation.getField(registry, "connections", Map.class).isEmpty());
    }

    @Test(expected = RuntimeException.class)
    public void test2Unbalanced() throws PostponeException {
        registry.setMaxConnectionsToHost(2);
        registry.openConnection(IP1);
        registry.openConnection(IP1);
        registry.releaseConnection(IP1);
        registry.releaseConnection(IP1);
        registry.releaseConnection(IP1);
    }
}
