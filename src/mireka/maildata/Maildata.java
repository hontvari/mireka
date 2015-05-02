package mireka.maildata;

import static mireka.util.CharsetUtil.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import mireka.maildata.io.DeferredFile;
import mireka.maildata.io.MaildataFile;
import mireka.maildata.io.TmpMaildataFile;
import mireka.maildata.parser.MaildataParser;
import mireka.util.CharsetUtil;
import mireka.util.StreamCopier;

/**
 * Maildata represents a message sent between computer users in the format of
 * Internet Message Format. An electronic mail consists of an envelope and the
 * content. This class represents the content.
 * 
 * @see <a href="https://tools.ietf.org/html/rfc5322">RFC 5322 - Internet
 *      Message Format</a>
 */
public class Maildata implements AutoCloseable {

    private final MaildataFile sourceFile;

    /**
     * Null if the mail is not yet parsed.
     */
    private MaildataParser.MaildataMap sourceFileMap;

    /**
     * It is initialized on demand, null if it is not yet initialized.
     */
    private HeaderSection headerSection;

    /**
     * It is initialized on demand, null if it is not yet initialized.
     */
    private SmartHeaderSection smartHeaderSection;

    private DeferredFile resultFile = null;

    /**
     * Creates a new Maildata object which will represent the Mail Data in the
     * MaildataFile. It saves the MaildataFile for later use.
     * 
     * @param sourceFile
     *            the MaildataFile which will be parsed by this object on
     *            demand. It will be closed by the close method of this object.
     *            It is guaranteed that this constructor does not start to read
     *            this sourceFile. The content of the sourceFile may be
     *            initialized later, but it must be initialized before a call to
     *            any other method of this Maildata class.
     */
    public Maildata(MaildataFile sourceFile) {
        this.sourceFile = sourceFile;
    }

    public HeaderSection getHeaders() throws IOException {
        if (headerSection == null) {
            if (sourceFileMap == null) {
                try (InputStream in = sourceFile.getInputStream()) {
                    sourceFileMap = new MaildataParser(in).parse();
                }
            }
            headerSection = sourceFileMap.headerSection;

        }
        return headerSection;
    }

    public SmartHeaderSection header() throws IOException {
        if (smartHeaderSection == null) {
            smartHeaderSection = new SmartHeaderSection(getHeaders());
        }
        return smartHeaderSection;
    }

    public void writeTo(OutputStream out) throws IOException {
        if (isUpdated()) {
            writeUpdatedTo(out);
        } else {
            try (InputStream in = sourceFile.getInputStream()) {
                StreamCopier.writeInputStreamIntoOutputStream(in, out);
            }
        }
    }

    public InputStream getInputStream() throws IOException {
        if (isUpdated()) {
            // previous instance even if exists, may be obsolete
            if (resultFile != null)
                resultFile.close();
            resultFile = new DeferredFile();
            try (OutputStream out = resultFile.getOutputStream()) {
                writeTo(out);
            }
            return resultFile.getInputStream();
        } else {
            return sourceFile.getInputStream();
        }
    }

    public Maildata copy() throws IOException {
        TmpMaildataFile tmpMaildataFile = new TmpMaildataFile();
        try (OutputStream out = tmpMaildataFile.deferredFile.getOutputStream()) {
            writeTo(out);
        }
        return new Maildata(tmpMaildataFile);
    }

    /**
     * Returns true if some part of this mail data has been updated, indicating
     * that the source MailData does not reflect the current state.
     */
    private boolean isUpdated() {
        return headerSection != null ? headerSection.isUpdated : false;
    }

    private void writeUpdatedTo(OutputStream out) throws IOException {
        for (Iterator<HeaderSection.Entry> it = getHeaders().entries(); it
                .hasNext();) {
            HeaderSection.Entry entry = it.next();
            if (entry.source == null) {
                String fieldAsString = entry.parsedField.generate();
                out.write(CharsetUtil.toAsciiBytes(fieldAsString));
            } else {
                out.write(toAsciiBytes(entry.source.originalSpelling));
            }
        }
        out.write(toAsciiBytes(sourceFileMap.separator));

        try (InputStream bodyInputStream = sourceFile.getInputStream()) {
            bodyInputStream.skip(sourceFileMap.bodyPosition);
            StreamCopier.writeInputStreamIntoOutputStream(bodyInputStream, out);
        }
    }

    /**
     * Releases system resources associated with this object.
     */
    @Override
    public void close() {
        if (sourceFile != null)
            sourceFile.close();
        if (resultFile != null)
            resultFile.close();
    }
}
