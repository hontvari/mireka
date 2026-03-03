package mireka.imap.server;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.concurrent.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mireka.imap.CompletionException;
import mireka.imap.Session;
import mireka.imap.SessionState;
import mireka.imap.command.Command;
import mireka.imap.command.FetchCommand;
import mireka.imap.command.LogoutCommand;
import mireka.imap.command.SearchCommand;
import mireka.imap.command.StoreCommand;
import mireka.imap.parser.Generator;
import mireka.imap.parser.ProtocolException;
import mireka.imap.update.ExpungeUpdate;
import mireka.imap.update.Update;

/**
 * Concurrency construct for managing concurrently running commands within the same connection.
 */
public class ConcurrentCommands {
    private final Logger logger = LoggerFactory.getLogger(ConcurrentCommands.class);
    private final Connection connection;
    private final Session session;
    private final Generator out;
    @GuardedBy("itself")
    private final Set<Update> updates = new LinkedHashSet<>();
    private final Object shutdownLock = new Object();
    @GuardedBy("shutdownLock")
    private boolean isShutdown;
    @GuardedBy("shutdownLock")
    private int runningCommands = 0;
    private final Lock inProgressLock = new ReentrantLock();
    private final Condition commandInProgressFinishing = inProgressLock.newCondition();
    @GuardedBy("inProgressLock")
    private LinkedHashSet<Command> inProgress = new LinkedHashSet<>();
    /**
     * true if the IDLE command is running and the client is ready to accept any notifications.
     */
    @GuardedBy("inProgressLock")
    private boolean isIdling = false;

    public ConcurrentCommands(Connection connection) {
        this.connection = connection;
        this.out = new Generator(connection.outputStream.createSubstream(),
                connection.protocolLogger);
        this.session = connection.session;
    }

    /**
     * After the function returns (without throwing an exception) the command is considered to be
     * running. These commands must be waited in case of a connection shutdown.
     * 
     * @throws CompletionException if the connection is shutting down.
     */
    public void registerRunning() throws CompletionException {
        synchronized (shutdownLock) {
            if (isShutdown)
                throw new CompletionException("Connection is terminating");
            runningCommands++;
        }
    }

    /**
     * It must be called after the execution of the command is ended in any way.
     */
    public void unregisterRunning() {
        synchronized (shutdownLock) {
            runningCommands--;
            shutdownLock.notifyAll();
        }
    }

    /**
     * Adds the command to the list of in progress commands. It must be called after the command is
     * received completely (including continuations), within the connection thread and it blocks
     * until the command can be executed without causing ambiguity between concurrently running
     * commands. If this function throws an exception, then the command will not be registered as in
     * progress. If the command is already registered it does nothing.
     * 
     * @throws CompletionException if a LOGOUT command is already running or completed, or the
     * thread is interrupted.
     * @throws ProtocolException if the command is not allowed in the current IMAP session state.
     */
    public void registerInProgress(Command command) throws ProtocolException, CompletionException {
        try {
            inProgressLock.lockInterruptibly();
            try {
                if (connection.session.state == SessionState.LOGOUT)
                    throw new CompletionException("Connection is in logout state");
                if (inProgress.stream().anyMatch(LogoutCommand.class::isInstance))
                    throw new CompletionException("Logout command already received");
                while (!command.isStartable(inProgress))
                    commandInProgressFinishing.await();
                if (!command.allowed().contains(connection.session.state))
                    throw new ProtocolException("Command is not allowed in this state");
                inProgress.add(command);
            } finally {
                inProgressLock.unlock();
            }
        } catch (InterruptedException e) {
            throw new CompletionException("Connection is shutting down");
        }
    }

    /**
     * Removes the command from the list of in progress commands. It must be called after the
     * completion response is sent. If the command is already unregistered it does nothing.
     */
    public void unregisterInProgress(Command command) throws IOException {
        inProgressLock.lock();
        try {
            inProgress.remove(command);
            commandInProgressFinishing.signalAll();
        } finally {
            inProgressLock.unlock();
        }
    }

    /**
     * Sends pending update notifications at the end of a command. It must be called before the
     * completion response sent.
     * 
     * Unsolicited EXPUNGE can only be sent at end of commands, not when no command is in progress.
     * EXPUNGE cannot be sent (not even at the end/while in progress)? any of FETCH, STORE, SEARCH
     * commands.
     */
    public void sendUpdatesAtEndOfCommand() throws IOException {
        inProgressLock.lock();
        try {
            Iterator<Update> it = updates.iterator();
            while (it.hasNext()) {
                Update u = it.next();
                if (!(u instanceof ExpungeUpdate && isFetchStoreSearchInProgress())) {
                    u.send(out, session);
                    it.remove();
                }
            }
        } finally {
            inProgressLock.unlock();
        }
    }

    private boolean isFetchStoreSearchInProgress() {
        return inProgress.stream().anyMatch(c -> c instanceof FetchCommand
                || c instanceof StoreCommand || c instanceof SearchCommand);
    }

    /**
     * Sends the unsolicited update notification either immediately if possible or put it into a
     * queue for later delivery at the end of the next command.
     * 
     * It is called by the mailbox, possibly from a different thread, it is thread safe.
     */
    public void sendOrQueue(Update update) {
        inProgressLock.lock();
        try {
            if (isIdling || (inProgress.isEmpty() && !(update instanceof ExpungeUpdate))) {
                try {
                    update.send(out, session);
                } catch (IOException e) {
                    logger.debug("Update notification cannot be sent, swallowing it, "
                            + "connection is broken", e);
                }
            } else {
                // First remove an old version if it exists, otherwise "add" has no effect.
                // It is assumed, that the later update is indeed more recent.
                updates.remove(update);
                updates.add(update);
            }
        } finally {
            inProgressLock.unlock();
        }
    }

    /**
     * After calling this no further commands will be accepted, specifically
     * {@link #registerRunning()} will throw a completion exception.
     */
    public void shutdown() {
        synchronized (shutdownLock) {
            isShutdown = true;
        }
    }

    public void awaitTermination() throws InterruptedException {
        synchronized (shutdownLock) {
            while (runningCommands > 0) {
                shutdownLock.wait();
            }
        }
    }

    /**
     * This will run after connection shutdown is initiated and after all running commands
     * completes.
     */
    public void sendUpdatesBeforeShutdown() {
        // TODO Auto-generated method stub

    }

    public void registerIdling() {
        inProgressLock.lock();
        isIdling = true;
        inProgressLock.unlock();
    }

    public void clearIdling() {
        inProgressLock.lock();
        isIdling = false;
        inProgressLock.unlock();
    }

}
