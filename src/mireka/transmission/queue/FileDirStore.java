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
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

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
     * allowed count of mails in the store
     */
    private int maxSize = 2000;
    private final AtomicInteger size = new AtomicInteger();

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
    public MailName[] initializeAndQueryMailNamesOrderedBySchedule()
            throws QueueStorageException {
        try {
            MailName[] mailNames = queryMailNames();
            size.set(mailNames.length);
            logger.info("Mail store initialized with " + size + " mails in "
                    + dir);
            return mailNames;
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
        int sizeValue = size.incrementAndGet();
        try {
            if (sizeValue > maxSize)
                throw new QueueStorageException(
                        "Store is full",
                        EnhancedStatus.TRANSIENT_SYSTEM_NOT_ACCEPTING_NETWORK_MESSAGES);
            return saveInner(srcMail);
        } catch (QueueStorageException e) {
            size.decrementAndGet(); // revert
            throw e;
        } catch (RuntimeException e) {
            size.decrementAndGet(); // revert
            throw e;
        }
    }

    private MailName saveInner(Mail srcMail) throws QueueStorageException {
        MailName mailName;
        try {
            mailName = allocateMessageContentFile(srcMail);
            writeMessageContentIntoFile(srcMail.mailData,
                    contentFileForName(mailName));
        } catch (IOException e) {
            throw new QueueStorageException(e, EnhancedStatus.MAIL_SYSTEM_FULL);
        }
        try {
            writeEnvelopeIntoFile(srcMail, envelopeFileForName(mailName));
        } catch (IOException e) {
            File mailDataFile = new File(dir, mailName.contentFileName());
            boolean fSuccess = mailDataFile.delete();
            if (!fSuccess)
                logger.error("Deleting message content file of " + mailName
                        + " failed after "
                        + "message envelope cannot be written into file. "
                        + "The mail transaction will be rejected, "
                        + "and the mail will never be processed. "
                        + "Message content file remains in the queue "
                        + "directory, " + "please delete it manually. " + "");
            throw new QueueStorageException(e, EnhancedStatus.MAIL_SYSTEM_FULL);
        }
        logger.debug("Mail was saved to store: {}, {}", srcMail, dir);
        return mailName;
    }

    private MailName allocateMessageContentFile(Mail srcMail)
            throws IOException {
        MailName mailName = MailName.create(srcMail.scheduleDate);
        while (true) {
            File file = contentFileForName(mailName);
            if (file.createNewFile())
                return mailName;
            mailName = mailName.nextInSequence();
            if (mailName.sequenceNumber >= 5)
                logger.warn("Too much attempt to create unique "
                        + "mail content file for name {}", mailName);
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
        try {
            File errorDir = new File(dir, "error");
            errorDir.mkdir();

            File envelopeFile = new File(dir, mailName.envelopeFileName());
            File envelopeTargetFile =
                    new File(errorDir, mailName.envelopeFileName());
            StreamCopier.copyFile(envelopeFile, envelopeTargetFile);

            File mailDataFile = new File(dir, mailName.contentFileName());
            File mailDataTargetFile =
                    new File(errorDir, mailName.contentFileName());
            StreamCopier.copyFile(mailDataFile, mailDataTargetFile);

            envelopeFile.delete();
            mailDataFile.delete();
            size.decrementAndGet();
        } catch (IOException e) {
            throw new QueueStorageException(e, EnhancedStatus.MAIL_SYSTEM_FULL);
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
            size.decrementAndGet();
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
    public void setDir(File dir) {
        this.dir = dir;
    }

    /**
     * @category GETSET
     */
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
}
