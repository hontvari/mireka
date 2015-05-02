package mireka.filter.misc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import mireka.filter.RecipientContext;
import mireka.filter.StatelessFilterType;
import mireka.maildata.Maildata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SavePostmasterMail extends StatelessFilterType {
    private final Logger logger = LoggerFactory
            .getLogger(SavePostmasterMail.class);
    private File dir;

    @Override
    public void dataRecipient(Maildata data, RecipientContext recipientContext) {
        if (!recipientContext.recipient.isPostmaster())
            return;
        try {
            File destFile = File.createTempFile("mail", ".txt", dir);
            try (OutputStream out = new FileOutputStream(destFile);
                    InputStream in = data.getInputStream()) {

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
