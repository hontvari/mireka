package mireka.maildata.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * TmpMaildataFile stores message content in memory if it is short or in a
 * temporary file if it is long.
 */
public class TmpMaildataFile implements MaildataSource {
    public final DeferredFile deferredFile;

    /**
     * Constructs a new TmpMaildataFile with an empty DeferredFile buffer.
     * Before this object can be really used, that is before the first call to
     * getInputStream, the buffer must be completely filled. For that the caller
     * must use {@link DeferredFile#getOutputStream()} on the
     * {@link #deferredFile} field.
     */
    public TmpMaildataFile() {
        deferredFile = new DeferredFile();
    }

    /**
     * Constructs a new TmpMaildataFile which will return the Mail Data bytes
     * from the specified stream.
     * 
     * Remark: the temporary file must be closed at this point, or at least
     * before getInputStream first called, in order to avoid unexpected disk
     * full errors while starting to read.
     * 
     * @param deferredFile
     *            The virtual file containing the message content.
     */
    public TmpMaildataFile(DeferredFile deferredFile) {
        this.deferredFile = deferredFile;
    }

    @Override
    public MaildataInputStream getInputStream(Range range) throws MaildataReadException {
        try {
            InputStream in = deferredFile.getInputStream();
            in.skip(range.start);
            return new MaildataInputStream(new Subsource(this, range), in);
        } catch (IOException e) {
            throw new MaildataReadException(e);
        }
    }

    @Override
    public long length() throws MaildataReadException {
        return deferredFile.length();
    }

    @Override
    public void close() {
        deferredFile.close();
    }

}
