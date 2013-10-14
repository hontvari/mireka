package mireka.transmission.immediate;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import mireka.smtp.client.BackendServer;

import org.junit.Test;

public class UpstreamTest {

    private BackendServer server66 = new BackendServer();
    private BackendServer server33 = new BackendServer();
    private BackendServer backupServer66 = new BackendServer();
    private BackendServer backupServer33 = new BackendServer();

    public UpstreamTest() {
        server66.setWeight(66);
        server66.setHost("server66");
        server33.setWeight(33);
        server33.setHost("server33");
        backupServer66.setWeight(66);
        backupServer66.setBackup(true);
        backupServer66.setHost("backup66");
        backupServer33.setWeight(33);
        backupServer33.setBackup(true);
        backupServer33.setHost("backup33");
    }

    @Test
    public void testOrderedServerList() {
        testList(server66, server33, backupServer66, backupServer33);
        testList(backupServer33, server33, backupServer66, server66);
    }

    private void testList(BackendServer... server) {
        Upstream upstream = new Upstream();
        upstream.setServers(Arrays.asList(server));
        int[] counters = new int[4];
        for (int i = 0; i < 1000; i++) {
            List<BackendServer> list = upstream.orderedServerList();
            System.out.println(list);
            if (list.get(0) == server66)
                counters[0]++;
            if (list.get(1) == server33)
                counters[1]++;
            if (list.get(2) == backupServer66)
                counters[2]++;
            if (list.get(3) == backupServer33)
                counters[3]++;
            assertTrue(list.get(2) == backupServer66
                    || list.get(3) == backupServer66);
            assertTrue(list.get(2) == backupServer33
                    || list.get(3) == backupServer33);
        }
        assertTrue(counters[0] > 600);
        assertTrue(counters[1] > 600);
        assertTrue(counters[2] > 600);
        assertTrue(counters[3] > 600);
        System.out.println(Arrays.toString(counters));
    }

}
