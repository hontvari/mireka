package mireka.transmission.queue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Arrays;
import java.util.NavigableSet;
import java.util.Properties;
import java.util.TreeSet;

import javax.annotation.concurrent.GuardedBy;

import mireka.MailData;
import mireka.smtp.EnhancedStatus;
import mireka.transmission.Mail;
import mireka.transmission.queue.dataprop.DataProperties;
import mireka.util.StreamCopier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FileDirStore stores scheduled mails in the file system in a single directory.
 * Mails are stored in two files. A properties file contains the envelope
 * information and a binary file contains the message content. In order to
 * provide some consistency in case of a system failure, the two files are
 * created and deleted in a specific order. On creation first the message
 * content file is saved, then the properties file. On deletion the order is the
 * opposite, the properties file is deleted first. The two files have the same
 * name but with different extension (.properties and .eml). The name is the
 * scheduled date with an additional serial number if it is necessary, so it
 * become a unique.
 */
public class FileDirStore {
    private final Logger logger = LoggerFactory.getLogger(FileDirStore.class);
    private File dir;
    /**
     * The allowed count of mails in the store.
     */
    private int maxSize = 2000;
    /**
     * The mail names currently allocated, in the usual circumstances these
     * corresponds to the mails currently scheduled and are residing in the
     * directory. If a mail for some reason cannot be deleted, then its name
     * will remain in this collection, until a system restart.
     */
    @GuardedBy("this")
    private final NavigableSet<MailName> mailNames = new TreeSet<MailName>();
    /**
     * True if {@link #initializeAndQueryMailNamesOrderedBySchedule()} is called
     * and it was successful. This must be the first operation which is called
     * on a new instance.
     */
    @GuardedBy("this")
    private boolean initialized;

    /**
     * use this constructor with setters
     */
    public FileDirStore() {
        // nothing to do
    }

    public FileDirStore(File dir, int maxQueueSize) {
        this.dir = dir;
        this.maxSize = maxQueueSize;
    }

    /**
     * this function must be called before any other method, and it cannot be
     * called more then once.
     * 
     * @throws QueueStorageException
     *             if the store cannot be initialized for some reason.
     */
    public synchronized MailName[] initializeAndQueryMailNamesOrderedBySchedule()
            throws QueueStorageException {
        try {
            MailName[] mailNamesArray = queryMailNames();
            mailNames.addAll(Arrays.asList(mailNamesArray));
            initialized = true;
            logger.info("Mail store initialized with " + mailNamesArray.length
                    + " mails in " + dir);
            return mailNamesArray;
        } catch (IOException e) {
            throw new QueueStorageException(e,
                    EnhancedStatus.TRANSIENT_LOCAL_ERROR_IN_PROCESSING);
        }
    }

    private MailName[] queryMailNames() throws IOException {
        String[] names = listEnvelopeFileNames();
        MailName[] mailNames = convertFileNamesToMailNames(names);
        Arrays.sort(mailNames);
        return mailNames;
    }

    private String[] listEnvelopeFileNames() throws IOException {
        String[] names = dir.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(MailName.MESSAGE_ENVELOPE_DOT_EXTENSION);
            }
        });
        if (names == null)
            throw new IOException("Cannot list directory: " + dir);
        return names;
    }

    private MailName[] convertFileNamesToMailNames(String[] names) {
        MailName[] mailNames = new MailName[names.length];
        for (int i = 0; i < names.length; i++) {
            mailNames[i] = new MailName(names[i]);
        }
        return mailNames;
    }

    public MailName save(Mail srcMail) throws QueueStorageException {
        MailName mailName = allocateMailName(srcMail);
        File contentFile = contentFileForName(mailName);
        File envelopeFile = envelopeFileForName(mailName);
        try {
            writeMessageContentIntoFile(srcMail.mailData,
                    contentFileForName(mailName));
        } catch (IOException e) {
            if (contentFile.exists() && !contentFile.delete()) {
                logger.error("Writing to the message content file failed, the "
                        + "file exists, and it could not be deleted: "
                        + contentFile);
            } else {
                releaseMailName(mailName);
            }
            throw new QueueStorageException(e, EnhancedStatus.MAIL_SYSTEM_FULL);
        }
        try {
            writeEnvelopeIntoFile(srcMail, envelopeFile);
        } catch (IOException e) {
            if (envelopeFile.exists() && !envelopeFile.delete()) {
                logger.error("Writing to the envelope file failed, the "
                        + "file exists, and it could not be deleted: "
                        + envelopeFile);
            } else {
                if (contentFile.exists() && !contentFile.delete()) {
                    logger.error("Deleting message content file of "
                            + mailName
                            + " failed after "
                            + "message envelope could not be written into file. "
                            + "The mail transaction will be rejected, "
                            + "and the mail will never be processed. "
                            + "Message content file remains in the queue "
                            + "directory, please delete it manually.");
                } else {
                    releaseMailName(mailName);
                }
            }
            throw new QueueStorageException(e, EnhancedStatus.MAIL_SYSTEM_FULL);
        }
        logger.debug("Mail was saved to store: {}, {}", srcMail, dir);
        return mailName;
    }

    private synchronized MailName allocateMailName(Mail srcMail)
            throws QueueStorageException {
        if (!initialized)
            throw new IllegalStateException();
        if (srcMail.scheduleDate == null)
            throw new IllegalArgumentException(
                    "Schedule date must have been set before");
        if (mailNames.size() >= maxSize)
            throw new QueueStorageException(
                    "Store is full",
                    EnhancedStatus.TRANSIENT_SYSTEM_NOT_ACCEPTING_NETWORK_MESSAGES);

        // find a free sequence number, within the scheduleDate
        long scheduleDate = srcMail.scheduleDate.getTime();
        MailName nameForTheNextTimePoint = new MailName(scheduleDate + 1, 0);
        MailName previousMail = mailNames.lower(nameForTheNextTimePoint);
        int sequenceNumber;
        if (previousMail == null || previousMail.scheduleDate < scheduleDate) {
            // so there is no mail on the same scheduleDate
            sequenceNumber = 0;
        } else {
            // there is mail on the same time point
            sequenceNumber = previousMail.sequenceNumber + 1;
        }
        MailName mailName = new MailName(scheduleDate, sequenceNumber);
        mailNames.add(mailName);
        return mailName;
    }

    private synchronized void releaseMailName(MailName mailName) {
        boolean found = mailNames.remove(mailName);
        if (!found) {
            logger.error("Mail name could not been found in the set of "
                    + "allocated names, this should not happen.");
        }
    }

    private File contentFileForName(MailName mailName) {
        return new File(dir, mailName.contentFileName());
    }

    private File envelopeFileForName(MailName mailName) {
        return new File(dir, mailName.envelopeFileName());
    }

    private void writeMessageContentIntoFile(MailData mailData,
            File messageContentFile) throws IOException {
        FileOutputStream out = new FileOutputStream(messageContentFile);
        try {
            mailData.writeTo(out);
        } finally {
            out.close();
        }
    }

    private void writeEnvelopeIntoFile(Mail srcMail, File propertiesFile)
            throws IOException {
        Properties props =
                new MailEnvelopePersister().saveToProperties(srcMail);
        Writer out =
                new OutputStreamWriter(new FileOutputStream(propertiesFile),
                        "UTF-8");
        try {
            props.store(out, null);
        } finally {
            out.close();
        }
    }

    public Mail read(MailName mailName) throws QueueStorageException {
        try {
            File propertiesFile = new File(dir, mailName.envelopeFileName());
            InputStream propertiesStream = new FileInputStream(propertiesFile);
            DataProperties properties = new DataProperties();
            try {
                properties
                        .load(new InputStreamReader(propertiesStream, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e); // impossible
            } finally {
                propertiesStream.close();
            }
            Mail mail =
                    new MailEnvelopePersister().readFromProperties(properties);

            File mailDataFile = new File(dir, mailName.contentFileName());
            mail.mailData = new FileMailData(mailDataFile);
            return mail;
        } catch (IOException e) {
            throw new QueueStorageException(e,
                    EnhancedStatus.TRANSIENT_LOCAL_ERROR_IN_PROCESSING);
        }
    }

    public void moveToErrorDir(MailName mailName) throws QueueStorageException {
        File envelopeFile = new File(dir, mailName.envelopeFileName());
        File mailDataFile = new File(dir, mailName.contentFileName());
        try {
            File errorDir = new File(dir, "error");
            errorDir.mkdir();

            if (envelopeFile.exists()) {
                File envelopeTargetFile =
                        new File(errorDir, mailName.envelopeFileName());
                StreamCopier.copyFile(envelopeFile, envelopeTargetFile);
                logger.info("Envelope file has been successfully copied into "
                        + "the error directory: " + envelopeFile);
            } else {
                logger.error("Envelope file could not be copied into the error "
                        + "directory, because it does not exist: "
                        + envelopeFile);
            }

            if (mailDataFile.exists()) {
                File mailDataTargetFile =
                        new File(errorDir, mailName.contentFileName());
                StreamCopier.copyFile(mailDataFile, mailDataTargetFile);
                logger.info("Mail data file has been successfully moved into "
                        + "the error directory: " + envelopeFile);
            } else {
                logger.error("Mail data file could not be moved to the error "
                        + "directory, because it does not exist: "
                        + mailDataFile);
            }

            envelopeFile.delete();
            mailDataFile.delete();
        } catch (IOException e) {
            throw new QueueStorageException(e, EnhancedStatus.MAIL_SYSTEM_FULL);
        }
        if (!envelopeFile.exists() && !mailDataFile.exists()) {
            releaseMailName(mailName);
        } else {
            throw new QueueStorageException(
                    "Mail name cannot be released, because either the "
                            + "envelope or the mail data file still exists: "
                            + mailName,
                    EnhancedStatus.TRANSIENT_LOCAL_ERROR_IN_PROCESSING);
        }
    }

    public void delete(MailName mailName) throws QueueStorageException {
        try {
            File envelopeFile = new File(dir, mailName.envelopeFileName());
            boolean fSuccess = envelopeFile.delete();
            if (!fSuccess)
                throw new IOException("Cannot delete envelope file "
                        + envelopeFile);
            File mailDataFile = new File(dir, mailName.contentFileName());
            fSuccess = mailDataFile.delete();
            if (!fSuccess)
                throw new IOException("Cannot delete mail data file "
                        + mailDataFile);
            releaseMailName(mailName);
        } catch (IOException e) {
            throw new QueueStorageException(e,
                    EnhancedStatus.TRANSIENT_LOCAL_ERROR_IN_PROCESSING);
        }
    }

    @Override
    public String toString() {
        return "FileDirStore [dir=" + dir + "]";
    }

    /**
     * @category GETSET
     */
    public void setDir(String dir) {
        this.dir = new File(dir);
    }

    /**
     * @category GETSET
     */
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
}
