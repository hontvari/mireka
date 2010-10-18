package mireka.filter.misc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.enterprise.context.Dependent;

import mireka.MailData;
import mireka.address.Recipient;
import mireka.filter.StatelessFilterType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Dependent
public class SavePostmasterMail extends StatelessFilterType {
    private final Logger logger =
            LoggerFactory.getLogger(SavePostmasterMail.class);
    private File dir;

    @Override
    public void dataRecipient(MailData data, Recipient recipient) {
        if (!recipient.isPostmaster())
            return;
        OutputStream out = null;
        try {
            File destFile = File.createTempFile("mail", ".txt", dir);
            out = new FileOutputStream(destFile);
            InputStream in = data.getInputStream();
            byte[] buffer = new byte[8192];
            int numRead;
            while ((numRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, numRead);
            }
        } catch (IOException e) {
            logger.error("Mail cannot be saved", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.warn("Mail content file cannot be closed", e);
                }
            }
        }
    }

    /**
     * @category GETSET
     */
    public File getDir() {
        return dir;
    }

    /**
     * @category GETSET
     */
    public void setDir(File dir) {
        this.dir = dir;
    }
}
