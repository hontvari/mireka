package mireka.pop.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.concurrent.GuardedBy;

import mireka.smtp.EnhancedStatus;
import mireka.transmission.LocalMailSystemException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maildrop provides the physical storage for a specific maildrop (assigned to a
 * user) using the file system. This implementation uses a single directory,
 * where it stores each mail in separate files in addition to some control
 * files. All operations are as atomic as possible, preventing leaving the
 * maildrop in an inconsistent state.
 */
public class Maildrop {
    private final Logger logger = LoggerFactory.getLogger(Maildrop.class);
    private final String name;
    private final File dir;
    @GuardedBy("this")
    private boolean isTransactionRunning;
    @GuardedBy("this")
    private int appenderCount;
    @GuardedBy("this")
    /**
     * true if data from disk is already loaded
     */
    private boolean isInitialized;
    /**
     * Sorted list of messages ordered by the numeric value of the file name.
     * Items must not be appended if a transaction is running.
     */
    @GuardedBy("this")
    private List<Message> messages;
    @GuardedBy("this")
    private final List<Message> pendingMessages =
            new ArrayList<Maildrop.Message>();
    @GuardedBy("this")
    private final UidManager uidManager;

    public Maildrop(String name, File maildropDir) {
        this.name = name;
        this.dir = maildropDir;
        this.uidManager = new UidManager(maildropDir);
    }

    public synchronized void beginTransaction() throws MaildropLockedException,
            MaildropPopException {
        if (isTransactionRunning)
            throw new MaildropLockedException();
        try {
            initialize();
        } catch (MaildropException e) {
            logger.error("Cannot read maildrop content from disk", e);
            throw new MaildropPopException("SYS/PERM", "Corrupted mailbox");
        }
        isTransactionRunning = true;
    }

    /**
     * Reads persistent maildrop data from the disk if it is not already read
     * 
     * @throws MaildropException
     */
    private void initialize() throws MaildropException {
        if (isInitialized)
            return;

        if (!isFullyConstructed())
            constructNewMaildropDir();

        uidManager.init();
        readDirectory();
        isInitialized = true;
    }

    private boolean isFullyConstructed() {
        return new File(dir, "constructed").exists();
    }

    private void constructNewMaildropDir() throws MaildropException {
        if (!dir.isDirectory()) {
            boolean success = dir.mkdir();
            if (!success)
                throw new MaildropException("Cannot create maildrop directory "
                        + dir);
        }
        uidManager.createInitialUidFile();
        File constructedFile = new File(dir, "constructed");
        try {
            boolean created = constructedFile.createNewFile();
            if (!created)
                throw new RuntimeException("Assertion failed");
        } catch (IOException e) {
            throw new MaildropException(
                    "The 'successful' marker file cannot be created, "
                            + "maildrop is invalid " + constructedFile);
        }
    }

    private void readDirectory() throws MaildropException {
        int countOfDeletedTemporaryFiles = 0;
        messages = new ArrayList<Message>();
        File[] files = dir.listFiles();
        if (files == null)
            throw new MaildropException(
                    "Cannot list mail files in maildrop at " + dir);
        for (File file : files) {
            String name = file.getName();
            if (name.startsWith("temp.mail.")) {
                boolean success = file.delete();
                if (success) {
                    countOfDeletedTemporaryFiles++;
                } else {
                    throw new MaildropException(
                            "Temporary mail file cannot be "
                                    + "deleted, maildrop is invalid " + file);
                }
            }
            if (!Character.isDigit(name.charAt(0)))
                continue;
            if (!name.endsWith(".eml"))
                throw new MaildropException("Invalid mail file name: " + file);
            String baseName = name.substring(0, name.length() - 4);
            Message message = new Message();
            try {
                message.id = Long.valueOf(baseName);
            } catch (NumberFormatException e) {
                throw new MaildropException("Invalid mail file name: " + file);
            }
            message.length = file.length();
            if (message.length == 0)
                throw new MaildropException("Cannot determine length of file "
                        + file);
            messages.add(message);
        }
        Collections.sort(messages, new MessageIdComparator());
        if (countOfDeletedTemporaryFiles > 0)
            logger.warn(countOfDeletedTemporaryFiles
                    + " temporary mail files were deleted in " + dir);
    }

    public synchronized long getCountOfMessages() {
        if (!isTransactionRunning)
            throw new IllegalStateException("Assertion failed");

        long result = 0;
        for (Message message : messages) {
            if (!message.deleted)
                result++;
        }
        return result;
    }

    public synchronized long getTotalOctets() {
        if (!isTransactionRunning)
            throw new IllegalStateException("Assertion failed");

        long result = 0;
        for (Message message : messages) {
            if (!message.deleted)
                result += message.length;
        }
        return result;
    }

    public synchronized List<ScanListing> getScanListings() {
        if (!isTransactionRunning)
            throw new IllegalStateException("Assertion failed");

        List<ScanListing> result = new ArrayList<ScanListing>();
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            if (!message.deleted)
                result.add(new ScanListing(i + 1, message.length));
        }
        return result;
    }

    public synchronized ScanListing getScanListing(int messageNumber)
            throws MaildropPopException, IllegalStateException {
        Message message = getMessageByMessageNumber(messageNumber);
        return new ScanListing(messageNumber, message.length);
    }

    public synchronized List<UidListing> getUidListings() {
        if (!isTransactionRunning)
            throw new IllegalStateException("Assertion failed");

        List<UidListing> result = new ArrayList<UidListing>();
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            if (!message.deleted)
                result.add(new UidListing(i + 1, message.id));
        }
        return result;
    }

    public synchronized UidListing getUidListing(int messageNumber)
            throws MaildropPopException, IllegalStateException {
        Message message = getMessageByMessageNumber(messageNumber);
        return new UidListing(messageNumber, message.id);
    }

    public synchronized InputStream getMailAsStream(int messageNumber)
            throws MaildropPopException, IllegalStateException {
        Message message = getMessageByMessageNumber(messageNumber);
        try {
            return new FileInputStream(message.getFile());
        } catch (FileNotFoundException e) {
            logger.error("Cannot open mail file " + message.getFile(), e);
            throw new MaildropPopException("SYS/PERM", "Corrupted mailbox");
        }
    }

    /**
     * Throws an exception if the maildrop is not in transaction mode, the
     * message number is invalid, or refers to a deleted mail.
     */
    private Message getMessageByMessageNumber(int messageNumber)
            throws MaildropPopException, IllegalStateException {
        if (!isTransactionRunning)
            throw new IllegalStateException();
        if (messageNumber < 1 || messageNumber > messages.size())
            throw new MaildropPopException(null, "No such message");
        Message message = messages.get(messageNumber - 1);
        if (message.deleted)
            throw new MaildropPopException(null, "Deleted message");
        return message;
    }

    public synchronized void delete(int messageNumber)
            throws MaildropPopException, IllegalStateException {
        Message message = getMessageByMessageNumber(messageNumber);
        message.deleted = true;
    }

    public synchronized void resetDeletions() {
        resetDeletedFlags();
    }

    private void resetDeletedFlags() {
        for (Message message : messages) {
            message.deleted = false;
        }
    }

    public synchronized void commitTransaction() throws MaildropPopException,
            IllegalStateException {
        if (!isTransactionRunning)
            throw new IllegalStateException();

        try {
            removeDeletedMessages();
        } finally {
            appendPendingMessages();
            isTransactionRunning = false;
        }
    }

    private void removeDeletedMessages() throws MaildropPopException {
        List<Message> keptMessages = new ArrayList<Maildrop.Message>();
        int cFailedDeletions = 0;
        for (Message message : messages) {
            if (message.deleted) {
                boolean success = message.getFile().delete();
                if (!success) {
                    message.deleted = false;
                    keptMessages.add(message);
                    cFailedDeletions++;
                    if (cFailedDeletions <= 3)
                        logger.error("Mail cannot be deleted: "
                                + message.getFile());
                }
            } else {
                keptMessages.add(message);
            }
        }
        messages = keptMessages;

        if (cFailedDeletions > 3)
            logger.error((cFailedDeletions - 3)
                    + " additional mails cannot be deleted");
        if (cFailedDeletions != 0)
            throw new MaildropPopException("SYS/PERM", "Cannot remove "
                    + cFailedDeletions + " deleted mails");
    }

    private void appendPendingMessages() {
        messages.addAll(pendingMessages);
        pendingMessages.clear();
    }

    public synchronized void rollbackTransaction() throws IllegalStateException {
        if (!isTransactionRunning)
            throw new IllegalStateException();

        resetDeletedFlags();
        appendPendingMessages();
        isTransactionRunning = false;
    }

    public synchronized MaildropAppender allocateAppender()
            throws LocalMailSystemException {
        try {
            initialize();
        } catch (MaildropException e) {
            throw new LocalMailSystemException(e,
                    EnhancedStatus.TRANSIENT_LOCAL_ERROR_IN_PROCESSING);
        }
        appenderCount++;
        return new Appender();
    }

    public synchronized void checkReleasedState() {
        if (isTransactionRunning)
            throw new IllegalStateException("Properly released maildrop was "
                    + "expected, but a transaction is still running: " + name
                    + ", " + dir);
        if (appenderCount >= 1)
            throw new IllegalStateException("Properly released maildrop was "
                    + "expected, but an appender is still not released: "
                    + name + ", " + dir);
    }

    /**
     * @category GETSET
     */
    public String getName() {
        return name;
    }

    private class Message {
        /**
         * UID
         */
        long id;
        /**
         * Mail length in bytes.
         */
        long length;
        /**
         * True if the mail is marked for deletion in the currently running
         * transaction.
         */
        boolean deleted;

        File getFile() {
            return new File(dir, id + ".eml");
        }
    }

    /**
     * This class compares {@link Message} instances based on there UID.
     */
    private static class MessageIdComparator implements Comparator<Message> {

        @Override
        public int compare(Message o1, Message o2) {
            return Long.signum(o1.id - o2.id);
        }
    }

    private class Appender implements MaildropAppender {
        private AppenderStatus status = AppenderStatus.NEW;
        private long uid;
        private File tempFile;
        private File finalFile;
        private FileOutputStream outputStream;

        @Override
        public OutputStream getOutputStream() throws LocalMailSystemException {
            if (status != AppenderStatus.NEW)
                throw new IllegalStateException();
            synchronized (Maildrop.this) {
                try {
                    allocateFileNames();
                } catch (InvalidUidFileException e) {
                    throw new LocalMailSystemException(e,
                            EnhancedStatus.TRANSIENT_LOCAL_ERROR_IN_PROCESSING);
                }
            }
            try {
                outputStream = new FileOutputStream(tempFile);
            } catch (FileNotFoundException e) {
                throw new LocalMailSystemException(e,
                        EnhancedStatus.TRANSIENT_LOCAL_ERROR_IN_PROCESSING);
            }
            status = AppenderStatus.OPEN;
            return outputStream;
        }

        private void allocateFileNames() throws InvalidUidFileException {
            uid = uidManager.allocateUid();
            tempFile = new File(dir, "temp.mail." + uid + ".eml");
            finalFile = new File(dir, uid + ".eml");
        }

        @Override
        public void commit() throws LocalMailSystemException {
            if (status == AppenderStatus.CLOSED)
                return;
            try {
                if (status == AppenderStatus.NEW)
                    return;

                // status is OPEN
                try {
                    outputStream.close();
                } catch (IOException e) {
                    throw new LocalMailSystemException(e,
                            EnhancedStatus.TRANSIENT_LOCAL_ERROR_IN_PROCESSING);
                }
                Message message = new Message();
                message.id = uid;
                message.length = tempFile.length();
                if (message.length == 0)
                    throw new LocalMailSystemException(
                            "Cannot retrieve file length " + tempFile,
                            EnhancedStatus.TRANSIENT_LOCAL_ERROR_IN_PROCESSING);
                boolean success = tempFile.renameTo(finalFile);
                if (!success) {
                    if (tempFile.delete()) {
                        throw new LocalMailSystemException(
                                "Cannot move temporary file to final "
                                        + "destination, but at least it could be deleted "
                                        + tempFile,
                                EnhancedStatus.TRANSIENT_LOCAL_ERROR_IN_PROCESSING);
                    } else {
                        throw new LocalMailSystemException(
                                "Cannot move temporary file to final destination, nor it can be deleted "
                                        + tempFile,
                                EnhancedStatus.TRANSIENT_LOCAL_ERROR_IN_PROCESSING);
                    }
                }
                synchronized (Maildrop.this) {
                    pendingMessages.add(message);
                    if (!isTransactionRunning) {
                        appendPendingMessages();
                    }
                }
                logger.debug("Message " + message.id + " is added to maildrop "
                        + name);
            } finally {
                synchronized (Maildrop.this) {
                    if (appenderCount < 1)
                        throw new RuntimeException("Assertion failed");
                    appenderCount--;
                }
                status = AppenderStatus.CLOSED;
            }
        }

        public void rollback() {
            if (status == AppenderStatus.CLOSED)
                return;
            try {
                if (status == AppenderStatus.NEW)
                    return;

                // status is OPEN
                try {
                    outputStream.close();
                } catch (IOException e) {
                    logger.error("Cannot close temporary file " + tempFile, e);
                    return;
                }
                boolean success = tempFile.delete();
                if (!success) {
                    logger.error("Cannot delete temporary file " + tempFile);
                    return;
                }
                logger.debug("Maildrop '" + name
                        + "' appender transaction for UID " + uid
                        + " is rolled back, temporary file is deleted.");
                return;
            } finally {
                synchronized (Maildrop.this) {
                    if (appenderCount < 1)
                        throw new RuntimeException("Assertion failed");
                    appenderCount--;
                }
                status = AppenderStatus.CLOSED;
            }

        }
    }

    private enum AppenderStatus {
        NEW, OPEN, CLOSED
    };
}
