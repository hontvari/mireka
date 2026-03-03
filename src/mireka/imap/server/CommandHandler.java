package mireka.imap.server;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mireka.imap.Completion;
import mireka.imap.ImapException;
import mireka.imap.Session;
import mireka.imap.command.AppendCommand;
import mireka.imap.command.AuthenticateCommand;
import mireka.imap.command.CapabilityCommand;
import mireka.imap.command.CloseCommand;
import mireka.imap.command.Command;
import mireka.imap.command.CopyCommand;
import mireka.imap.command.CreateCommand;
import mireka.imap.command.EnableCommand;
import mireka.imap.command.ExpungeCommand;
import mireka.imap.command.FetchCommand;
import mireka.imap.command.IdleCommand;
import mireka.imap.command.ListCommand;
import mireka.imap.command.LoginCommand;
import mireka.imap.command.LogoutCommand;
import mireka.imap.command.LsubCommand;
import mireka.imap.command.NamespaceCommand;
import mireka.imap.command.NoopCommand;
import mireka.imap.command.SelectExamineCommand;
import mireka.imap.command.StoreCommand;
import mireka.imap.command.SubscribeCommand;
import mireka.imap.command.UidCommand;
import mireka.imap.command.UnselectCommand;
import mireka.imap.parser.CommandParser;
import mireka.imap.parser.CommandSyntaxException;
import mireka.imap.parser.NoMoreLinesException;
import mireka.imap.parser.UnknownCommandException;
import mireka.imap.store.Transaction;

/**
 * This class selects and runs the {@link Command} corresponding to the next IMAP command line it
 * receives from the client, moreover it processes the connection open and close events.
 */
class CommandHandler {
    private final Logger logger = LoggerFactory.getLogger(CommandHandler.class);
    private final Session session;
    private final ThreadPoolExecutor executor;

    /**
     * Keys are upper case command names
     */
    private final Map<String, CommandFactory> commandMap = new HashMap<>();

    public CommandHandler(Session session) {
        this.session = session;
        int maxWorkers = Runtime.getRuntime().availableProcessors() * 2;
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(8);
        this.executor = new ThreadPoolExecutor(0, maxWorkers, 60, SECONDS, queue,
                new WorkerThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
        commandMap.put("CAPABILITY", CapabilityCommand::new);
        commandMap.put("NOOP", NoopCommand::new);
        commandMap.put("LOGOUT", LogoutCommand::new);

        commandMap.put("AUTHENTICATE", AuthenticateCommand::new);
        commandMap.put("LOGIN", LoginCommand::new);

        commandMap.put("ENABLE", EnableCommand::new);
        commandMap.put("SELECT", SelectExamineCommand::new);
        commandMap.put("EXAMINE", SelectExamineCommand::new);
        commandMap.put("CREATE", CreateCommand::new);
        commandMap.put("SUBSCRIBE", SubscribeCommand::new);
        commandMap.put("LIST", ListCommand::new);
        commandMap.put("LSUB", LsubCommand::new);
        commandMap.put("NAMESPACE", NamespaceCommand::new);
        commandMap.put("APPEND", AppendCommand::new);
        commandMap.put("IDLE", IdleCommand::new);

        commandMap.put("CLOSE", CloseCommand::new);
        commandMap.put("UNSELECT", UnselectCommand::new);
        commandMap.put("EXPUNGE", ExpungeCommand::new);
        commandMap.put("FETCH", FetchCommand::new);
        commandMap.put("STORE", StoreCommand::new);
        commandMap.put("COPY", CopyCommand::new);
        commandMap.put("UID", UidCommand::new);
    }

    public void handleCommand() throws IOException, NoMoreLinesException {
        CommandParser parser = new CommandParser(session);
        boolean isRegisteredAsRunning = false, isSubmittedToExecutor = false;
        Command command = null;
        try {
            parser.extractCommand();
            CommandFactory commandFactory = commandMap.get(parser.command);
            if (commandFactory == null)
                throw new UnknownCommandException();
            command = commandFactory.supply(session, parser);
            session.commands.registerRunning();
            isRegisteredAsRunning = true;
            Transaction.init();
            session.server.getRepository().begin();
            command.parse();
            parser.parseEof();
            session.commands.registerInProgress(command);
            Completion result = command.execute();
            if (result.continueAsync) {
                executor.execute(new CommandTask(command));
                isSubmittedToExecutor = true;
            } else {
                Transaction.commit();
                session.commands.sendUpdatesAtEndOfCommand();
                command.respondCompletion(result);
                session.commands.unregisterInProgress(command);
            }
            if (result.logout)
                session.connection.shutdownNow(null);
        } catch (ImapException e) {
            parser.skip();
            if (e instanceof CommandSyntaxException)
                logger.trace("handleCommand failed", e);
            else
                logger.debug("handleCommand failed", e);
            String tag = parser == null || parser.tag == null ? "*" : parser.tag;
            session.connection.sendResponse(e.toResponse(tag));
            if (command != null)
                session.commands.unregisterInProgress(command);
        } finally {
            if (isRegisteredAsRunning && !isSubmittedToExecutor)
                session.commands.unregisterRunning();
            Transaction.cleanup();
        }
    }

    /**
     * Processes both expected and unexpected disconnections
     */
    public void connectionClosed() {
//        if (session.getSessionState() == SessionState.TRANSACTION) {
//            session.getMaildrop().rollbackTransaction();
//            logger.debug("Maildrop transaction is rolled back");
//        }
//        if (session.getMaildrop() != null)
//            session.getServer().getRepository()
//                    .releaseMaildrop(session.getMaildrop());
    }

    private class WorkerThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        WorkerThreadFactory() {
            Socket socket = session.connection.socket;
            String remote = socket.getInetAddress() + ":" + socket.getPort();
            namePrefix = "IMAP-worker-" + session.server.getName() + "-" + remote + "-";
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, namePrefix + threadNumber.getAndIncrement());
        }
    }

    private class CommandTask implements Runnable {
        final Command command;

        CommandTask(Command command) {
            this.command = command;
        }

        @Override
        public void run() {
            try {
                try {
                    Completion result = command.asyncExecute();
                    Transaction.commit();
                    session.commands.sendUpdatesAtEndOfCommand();
                    command.respondCompletion(result);
                    session.commands.unregisterInProgress(command);
                } catch (ImapException e) {
                    session.connection.sendResponse(e.toResponse(command.tag));
                    session.commands.unregisterInProgress(command);
                }
            } catch (IOException e) {
                session.connection.handleIoException(e);
            } finally {
                session.commands.unregisterRunning();
            }
        }

    }

    interface CommandFactory {
        Command supply(Session session, CommandParser parser);
    }
}
