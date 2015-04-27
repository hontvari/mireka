package mireka.dmarc;

import static org.junit.Assert.*;
import mireka.maildata.DotAtomDomainPart;

import org.junit.Ignore;
import org.junit.Test;

@Ignore("intergration test / external changes possible")
public class PolicyDiscoveryTest {

    @Test
    public void testYahoo() throws RecoverableDmarcException {
        DotAtomDomainPart domainPart = new DotAtomDomainPart("yahoo.com");
        PolicyRecord record = new PolicyDiscovery().discoverPolicy(domainPart);
        assertEquals(PolicyRecord.Request.reject, record.request);
    }

    @Test
    public void testGmail() throws RecoverableDmarcException {
        DotAtomDomainPart domainPart = new DotAtomDomainPart("gmail.com");
        PolicyRecord record = new PolicyDiscovery().discoverPolicy(domainPart);
        assertEquals(PolicyRecord.Request.none, record.request);
    }

    @Test
    public void testOutlook() throws RecoverableDmarcException {
        DotAtomDomainPart domainPart = new DotAtomDomainPart("outlook.com");
        PolicyRecord record = new PolicyDiscovery().discoverPolicy(domainPart);
        assertEquals(PolicyRecord.Request.none, record.request);
    }

    @Test
    public void testFlyOrDie() throws RecoverableDmarcException {
        DotAtomDomainPart domainPart = new DotAtomDomainPart("flyordie.com");
        PolicyRecord record = new PolicyDiscovery().discoverPolicy(domainPart);
        assertNull(record);
    }

}
