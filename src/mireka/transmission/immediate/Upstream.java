package mireka.transmission.immediate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mireka.smtp.client.BackendServer;

/**
 * An Upstream is a load balanced set of BackendServers. A specific server is
 * selected randomly but considering its weight and whether it is a backup or
 * primary server.
 */
public class Upstream {
    private List<BackendServer> servers;

    public List<BackendServer> orderedServerList() {
        List<BackendServer> shuffledServers = new ArrayList<>();
        shuffledServers.addAll(shuffle(getPrimaryServers()));
        shuffledServers.addAll(shuffle(getBackupServers()));
        return shuffledServers;
    }

    private List<BackendServer> getPrimaryServers() {
        List<BackendServer> result = new ArrayList<>();
        for (BackendServer server : servers) {
            if (!server.isBackup())
                result.add(server);
        }
        return result;
    }

    private List<BackendServer> getBackupServers() {
        List<BackendServer> result = new ArrayList<>();
        for (BackendServer server : servers) {
            if (server.isBackup())
                result.add(server);
        }
        return result;
    }

    private List<BackendServer> shuffle(List<BackendServer> servers) {
        List<BackendServer> shuffledList = new ArrayList<>();
        List<BackendServer> remainingServers = new ArrayList<>(servers);
        if (remainingServers.isEmpty())
            return Collections.emptyList();
        while (remainingServers.size() > 1) {
            int iSelected = selectFrom(remainingServers);
            BackendServer selected = remainingServers.remove(iSelected);
            shuffledList.add(selected);
        }
        shuffledList.add(remainingServers.get(0));
        return shuffledList;
    }

    private int selectFrom(List<BackendServer> servers) {
        double drawnNumber = Math.random() * totalWeight(servers);
        double weigthsUntilNow = 0;
        for (int i = 0; i < servers.size(); i++) {
            BackendServer sender = servers.get(i);
            weigthsUntilNow += sender.getWeight();
            if (drawnNumber < weigthsUntilNow)
                return i;
        }
        return servers.size() - 1;
    }

    private double totalWeight(List<BackendServer> servers) {
        double total = 0;
        for (BackendServer server : servers) {
            total += server.getWeight();
        }
        return total;
    }

    /** @x.category GETSET **/
    public List<BackendServer> getServers() {
        return servers;
    }

    /** @x.category GETSET **/
    public void setServers(List<BackendServer> servers) {
        this.servers = servers;
    }
}
