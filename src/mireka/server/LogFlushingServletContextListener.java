package mireka.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

/**
 * It flushes Logback output streams when the web application is shut down. This
 * servlet context listener should be installed if buffering is switched on on
 * at least one Logback logfile or stream.
 */
public class LogFlushingServletContextListener implements
        ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.stop();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // nothing to do
    }
}
