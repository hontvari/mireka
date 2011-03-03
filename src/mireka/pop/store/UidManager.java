package mireka.pop.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UidManager retrieves and updates the content of the file associated with each
 * maildrop, which contains the last allocated UID.
 */
class UidManager {
    private final Logger logger = LoggerFactory.getLogger(UidManager.class);
    private long highestAllocatedUid;
    private final File file;
    private final File tempFile;
    private boolean isInitialized;

    UidManager(File dir) {
        file = new File(dir, "uid.txt");
        tempFile = new File(dir, "temp.uid.txt");
    }

    void createInitialUidFile() throws InvalidUidFileException {
        if (file.exists()) {
            boolean success = file.delete();
            if (success) {
                logger.warn("Uid file already existed, "
                        + "it is successfully removed now " + file);
            } else {
                throw new InvalidUidFileException(
                        "Cannot remove already existing uid file, "
                                + "maildrop is invalid " + file);
            }
        }
        try {
            writeUidFile(file);
        } catch (InvalidUidFileException e) {
            throw new InvalidUidFileException(
                    "Initial uid file cannot be created, maildrop is invalid "
                            + file);
        }
    }

    private void writeUidFile(File file) throws InvalidUidFileException {
        Writer writer;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            throw new InvalidUidFileException("Cannot write uid file " + file,
                    e);
        }
        try {
            writer.append(highestAllocatedUid + "+");
        } catch (IOException e) {
            throw new InvalidUidFileException("Error while writing uid file "
                    + file, e);
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                throw new InvalidUidFileException("Cannot close uid file "
                        + file);
            }
        }
    }

    void init() throws InvalidUidFileException {

        try {
            highestAllocatedUid = readUidFile(file);
        } catch (InvalidUidFileException e) {
            logger.warn("Cannot find valid uid file, trying to recover", e);
            if (tempFile.exists()) {
                logger.warn("Temporary uid file is found");
                if (file.exists()) {
                    boolean success = file.delete();
                    if (success) {
                        logger.warn("Invalid original uid file is "
                                + "successfully deleted");
                    } else {
                        logger.error("Uid file does exist but it is "
                                + "invalid and it cannot "
                                + "be deleted, maildrop is invalid");
                        throw e;
                    }
                }
                boolean success = tempFile.renameTo(file);
                if (!success) {
                    logger.error("Cannot move temporary uid file, maildrop is invalid");
                    throw e;
                }
                try {
                    highestAllocatedUid = readUidFile(file);
                } catch (InvalidUidFileException e1) {
                    logger.error(
                            "Temporary uid file was invalid too, maildrop is invalid",
                            e1);
                    throw e;
                }
            } else {
                logger.error("No temporary uid file exists, maildrop is invalid");
                throw e;
            }
        }

        if (tempFile.exists()) {
            boolean success = tempFile.delete();
            if (!success)
                throw new InvalidUidFileException(
                        "Temporary uid file cannot be deleted, maildrop is invalid");
        }
        isInitialized = true;
    }

    private long readUidFile(File file) throws InvalidUidFileException {
        InputStreamReader reader;
        try {
            reader =
                    new InputStreamReader(new FileInputStream(file), "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Assertion failed");
        } catch (FileNotFoundException e) {
            throw new InvalidUidFileException(e);
        }
        try {
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[32];
            int cRead;
            while (-1 != (cRead = reader.read(buffer))) {
                builder.append(buffer, 0, cRead);
            }
            String content = builder.toString();
            if (content.length() < 2)
                throw new InvalidUidFileException("Uid file too short: " + file);
            if (content.charAt(content.length() - 1) != '+')
                throw new InvalidUidFileException(
                        "Uid file has no ending '+': " + file);
            try {
                return Long
                        .parseLong(content.substring(0, content.length() - 1));
            } catch (NumberFormatException e) {
                throw new InvalidUidFileException(
                        "Uid file does not contain number: " + file, e);
            }
        } catch (IOException e) {
            throw new InvalidUidFileException(e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                logger.warn("Cannot close uid file " + file, e);
            }
        }
    }

    long allocateUid() throws InvalidUidFileException {
        if (!isInitialized)
            throw new IllegalStateException();
        highestAllocatedUid++;
        writeUidFile(tempFile);
        boolean success = file.delete();
        if (!success)
            throw new InvalidUidFileException("Cannot delete uid file " + file);
        success = tempFile.renameTo(file);
        if (!success) {
            highestAllocatedUid--;
            throw new InvalidUidFileException(
                    "Cannot rename temporary file to final file " + tempFile);
        }
        return highestAllocatedUid;
    }
}
