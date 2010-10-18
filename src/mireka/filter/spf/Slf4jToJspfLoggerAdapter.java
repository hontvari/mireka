package mireka.filter.spf;

import org.apache.james.jspf.core.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jToJspfLoggerAdapter implements Logger {
    private final org.slf4j.Logger logger;

    public Slf4jToJspfLoggerAdapter() {
        logger = LoggerFactory.getLogger("org.apache.james.jspf");
    }

    private Slf4jToJspfLoggerAdapter(String name) {
        logger = LoggerFactory.getLogger(name);
    }

    @Override
    public void debug(String message) {
        logger.debug(message);
    }

    @Override
    public void debug(String message, Throwable throwable) {
        logger.debug(message, throwable);
    }

    @Override
    public void error(String message) {
        logger.error(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    @Override
    public void fatalError(String message) {
        logger.error(message);
    }

    @Override
    public void fatalError(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    @Override
    public Logger getChildLogger(String name) {
        return new Slf4jToJspfLoggerAdapter(logger.getName() + "." + name);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void info(String message, Throwable throwable) {
        logger.info(message, throwable);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public boolean isFatalErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        logger.warn(message, throwable);
    }
}
