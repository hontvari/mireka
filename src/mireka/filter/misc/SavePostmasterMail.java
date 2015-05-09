package mireka.filter.misc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import mireka.filter.MailTransaction;
import mireka.filter.RecipientContext;
import mireka.filter.StatelessFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SavePostmasterMail filter saves the mail data part of every mail whose
 * recipient is the postmaster into the configured folder for debugging
 * purposes.
 */
public class SavePostmasterMail extends StatelessFilter {
    private final Logger logger = LoggerFactory
            .getLogger(SavePostmasterMail.class);
    private File dir;

    @Override
    public void data(MailTransaction transaction) {
        for (RecipientContext recipientContext : transaction.recipientContexts) {
            if (recipientContext.recipient.isPostmaster()) {
                saveMaildata(transaction);
                return;
            }
        }
    }

    private void saveMaildata(MailTransaction transaction) {
        try {
            File destFile = File.createTempFile("mail", ".txt", dir);
            try (OutputStream out = new FileOutputStream(destFile);
                    InputStream in = transaction.data.getInputStream()) {

                byte[] buffer = new byte[8192];
                int numRead;
                while ((numRead = in.read(buffer)) > 0) {
                    out.write(buffer, 0, numRead);
                }
            }
        } catch (IOException e) {
            logger.error("Mail cannot be saved", e);
        }
    }

    /**
     * @x.category GETSET
     */
    public void setDir(String dir) {
        this.dir = new File(dir);
    }
}
