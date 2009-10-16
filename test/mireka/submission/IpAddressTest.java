package mireka.submission;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;

public class IpAddressTest {
    @Test
    public void testSingeIpAddressMatch() throws UnknownHostException {
        IpAddress specification = new IpAddress("192.0.2.0");
        assertTrue(specification.isSatisfiedBy(InetAddress
                .getByName("192.0.2.0")));
    }

    @Test
    public void testSingeIpAddressFail() throws UnknownHostException {
        IpAddress specification = new IpAddress("192.0.2.0");
        assertFalse(specification.isSatisfiedBy(InetAddress
                .getByName("192.0.2.1")));
    }

    @Test
    public void testRangeMatch() throws UnknownHostException {
        IpAddress specification = new IpAddress("192.0.2.0/28");
        assertTrue(specification.isSatisfiedBy(InetAddress
                .getByName("192.0.2.15")));

    }

    @Test
    public void testRangeFailPartialByte() throws UnknownHostException {
        IpAddress specification = new IpAddress("192.0.2.0/28");
        assertFalse(specification.isSatisfiedBy(InetAddress
                .getByName("192.0.2.16")));
    }

    @Test
    public void testRangeFailCompleteByte() throws UnknownHostException {
        IpAddress specification = new IpAddress("192.0.2.0/28");
        assertFalse(specification.isSatisfiedBy(InetAddress
                .getByName("192.168.2.0")));
    }
}
