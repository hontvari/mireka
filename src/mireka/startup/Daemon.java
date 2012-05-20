package mireka.startup;

/**
 * Startup class which is compatible with Apache Commons Daemon jsvc.
 */
public class Daemon {
    public void init(String[] arguments) {
        Start.main(arguments);
    }

    public void start() {

    }

    public void stop() {

    }

    public void destroy() {
        Stop.main(new String[0]);
    }
}
