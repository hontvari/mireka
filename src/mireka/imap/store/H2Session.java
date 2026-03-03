package mireka.imap.store;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mireka.imap.UnavailableException;

public class H2Session {
    private static final Logger logger = LoggerFactory.getLogger(H2Session.class);
    private static ThreadLocal<H2Session> instance = new ThreadLocal<>();
    private DataSource ds;
    /**
     * Null if the connection is not yet requested.
     */
    private Connection connection;

    private H2Session(DataSource ds) {
        this.ds = ds;
    }

    public static void init(DataSource ds) {
        H2Session s = instance.get();
        if (s != null)
            throw new IllegalStateException(s.toString());
        s = new H2Session(ds);
        instance.set(s);
        logger.trace("init {}", s);
    }

    public static Connection connection() throws UnavailableException {
        try {
            H2Session s = instance.get();
            if (s == null)
                throw new IllegalStateException();
            if (s.connection == null) {
                s.connection = s.ds.getConnection();
                logger.trace("begin db transaction");
                s.connection.setAutoCommit(false);
            }
            return s.connection;
        } catch (SQLException e) {
            throw new UnavailableException(e);
        }
    }

    public static void commit() throws UnavailableException {
        try {
            logger.trace("commit");
            H2Session s = instance.get();
            if (s == null)
                throw new IllegalStateException();
            if (s.connection == null)
                return;
            if (!s.connection.getAutoCommit()) {
                s.connection.commit();
                logger.trace("commit db transaction");
            }
            s.connection.close();
            s.connection = null;
        } catch (SQLException e) {
            // TODO: if the commit fails, all cached database value must be invalidated, e.g.
            // the list and status of mailboxes.
            throw new UnavailableException(e);
        }
    }

    public static void cleanup() {
        try {
            logger.trace("cleanup");
            H2Session s = instance.get();
            if (s == null)
                return;
            if (s.connection != null) {
                if (!s.connection.getAutoCommit()) {
                    logger.debug("Rolling back transaction");
                    s.connection.rollback();
                }
                s.connection.close();
                s.connection = null;
            }
        } catch (SQLException e) {
            logger.warn("SQL connection close failed", e);
        } finally {
            instance.remove();
        }
    }
}
