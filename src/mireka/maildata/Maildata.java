package mireka.maildata;

import java.io.IOException;
import java.io.OutputStream;

import mireka.maildata.io.MaildataSource;
import mireka.maildata.io.PositionOutputStream;
import mireka.maildata.io.TmpMaildataFile;

/**
 * Maildata represents a message sent between computer users in the format of Internet Message
 * Format. An electronic mail consists of an envelope and the content. This class represents the
 * content.
 * 
 * Header section related functions in this class represents the high level, semantic content of the
 * header section compared to the lower level header fields. For example it can return or set the
 * complete lists of authors. This is in contrast to the {@link HeaderSection}, returned by
 * {@link #headers()} which deals with individual header fields.
 * 
 * @see <a href="https://tools.ietf.org/html/rfc5322">RFC 5322 - Internet Message Format</a>
 */
public class Maildata extends Entity {

    public Maildata() {
        super();
    }

    public Maildata(MaildataSource source) {
        super(source);
    }

    /**
     * Create a copy of this message backed by memory or a temporary file. The caller must close the
     * returned instance to delete the temporary file.
     */
    public Maildata copy() throws IOException {
        TmpMaildataFile tmpMaildataFile = new TmpMaildataFile();
        try (OutputStream out = tmpMaildataFile.deferredFile.getOutputStream()) {
            writeTo(out);
        }
        return new Maildata(tmpMaildataFile);
    }

    /**
     * Save the current state of this message into a new {@link TmpMaildataFile} if this message has
     * been changed. The new file will be the new {@link MaildataSource} of this instance. The
     * updated state is cleared. The previous source will be closed.
     * 
     * @throws IOException
     */
    public void save() throws IOException {
        if (!isUpdated())
            return;
        MaildataSource oldsrc = source;
        TmpMaildataFile tmpMaildataFile = new TmpMaildataFile();
        try (OutputStream out = tmpMaildataFile.deferredFile.getOutputStream()) {
            save(new PositionOutputStream(tmpMaildataFile, out));
        }
        if (oldsrc != null)
            oldsrc.close();
    }

    /**
     * Releases system resources associated with this object.
     */
    @Override
    public void close() {
        super.close();
        if (source != null)
            source.close();
    }
}
