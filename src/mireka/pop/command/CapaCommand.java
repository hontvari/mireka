package mireka.pop.command;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.concurrent.GuardedBy;

import mireka.pop.Command;
import mireka.pop.CommandParser;
import mireka.pop.Session;

public class CapaCommand implements Command {
    @GuardedBy("CapaCommand.class")
    private static String cachedVersion;
    private final Session session;

    public CapaCommand(Session session) {
        this.session = session;
    }

    @Override
    public void execute(CommandParser commandParser) throws IOException {
        session.getThread().sendResponse("+OK Capability list follows");
        session.getThread().sendResponse("TOP");
        session.getThread().sendResponse("USER");
        session.getThread().sendResponse("RESP-CODES");
        session.getThread().sendResponse("AUTH-RESP-CODE");
        session.getThread().sendResponse("UIDL");
        session.getThread().sendResponse(
                "IMPLEMENTATION Mireka-" + getVersion());
        session.getThread().sendResponse(".");
    }

    private String getVersion() {
        // does not work (in Resin only?)
        // getClass().getPackage().getImplementationVersion()
        synchronized (getClass()) {
            if (cachedVersion == null) {
                try {
                    InputStream resourceAsStream =
                            getClass().getResourceAsStream(
                                    "/version.properties");
                    Properties properties = new Properties();
                    properties.load(resourceAsStream);
                    resourceAsStream.close();
                    cachedVersion = properties.getProperty("version");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return cachedVersion;
        }
    }
}
