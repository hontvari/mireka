package mireka.imap.server;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.concurrent.GuardedBy;

import mireka.imap.Session;

public class ConcurrentConnections {
    @GuardedBy("this")
    private final Set<Session> sessions = new HashSet<>(200);

    public synchronized void add(Session session) {
        sessions.add(session);
    }

    public synchronized void remove(Session sesson) {
        sessions.remove(sesson);
    }

    public synchronized void forEach(Consumer<Session> action) {
        sessions.forEach(action);
    }

    public synchronized Session[] getAll() {
        return sessions.toArray(Session[]::new);
    }

    public synchronized int size() {
        return sessions.size();
    }


}
