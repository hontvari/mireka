package mireka.maildata;

import static mireka.util.CharsetUtil.toAsciiBytes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.annotation.Nullable;

import mireka.maildata.io.DeferredFile;
import mireka.maildata.io.MaildataSource;
import mireka.maildata.io.TmpMaildataFile;
import mireka.maildata.parser.MaildataParser;
import mireka.util.CharsetUtil;
import mireka.util.StreamCopier;

/**
 * Maildata represents a message sent between computer users in the format of
 * Internet Message Format. An electronic mail consists of an envelope and the
 * content. This class represents the content.
 * 
 * Header section related functions in this class represents the high level,
 * semantic content of the header section compared to the lower level header
 * fields. For example it can return or set the complete lists of authors. This
 * is in contrast to the {@link HeaderSection}, returned by {@link #headers()}
 * which deals with individual header fields.
 * 
 * @see <a href="https://tools.ietf.org/html/rfc5322">RFC 5322 - Internet
 *      Message Format</a>
 */
public class Maildata2 implements AutoCloseable {
    /**
     * false, if this object represents a new mail, initially with empty data, instead of an
     * existing mail read from a source.
     */
    private final boolean isExisting;
    /**
     * This maildata is read from this source, null is this is a new mail.
     */
    @Nullable
    private final MaildataSource source;

    /**
     * Null if parsing is not initiated or if this is a new mail.
     */
    private MaildataParser parser;
    private boolean isHeaderParsed;
    private boolean isFullyParsed;

    /**
     * It may be empty if the mail is not yet parsed.
     */
    private final MailMap map = new MailMap();

    /**
     * This file is used to temporarily store the new or updated mail in a byte stream, client
     * objects requesting the mail in a byte stream format read the bytes from this storage object.
     */
    private DeferredFile resultFile = null;

    public final SimpleMaildata simple = new SimpleMaildata(this);

    /**
     * Creates a new mail which is initially empty.
     */
    public Maildata() {
        isExisting = false;
        source = null;
        map.headerSection = new HeaderSection();
    }
    
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
    public Maildata(MaildataSource sourceFile) {
        isExisting = true;
        this.source = sourceFile;
    }

    public HeaderSection headers() {
        return headStructure().headerSection;
    }

    public MailMap structure() {
        if (isExisting && !isFullyParsed) {
            if (parser == null)
                parser = new MaildataParser(source.getInputStream());
            if (isHeaderParsed) {
                if (map.separator != null)
                    parser.parseBodyAndEof(map);
            } else {
                parser.parse(map);
            }
            isFullyParsed = true;
            isHeaderParsed = true;
        }
        return map;
    }

    /**
     * Returns a MaildataMap with at least the heading and separator fields filled. This may be more
     * efficient if the body part is not required.
     */
    public MailMap headStructure() {
        if (isExisting && !isHeaderParsed) {
            if (parser == null) {
                parser = new MaildataParser(source.getInputStream());
            }
            parser.parseHeaderAndSeparator(map);
            isHeaderParsed = true;
        }
        return map;
    }

    public void writeTo(OutputStream out) throws IOException {
        if (isUpdated()) {
            writeUpdatedTo(out);
        } else {
            try (InputStream in = source.getInputStream()) {
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
            return source.getInputStream();
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
        return map.headerSection != null ? map.headerSection.isUpdated : false;
    }

    private void writeUpdatedTo(OutputStream out) throws IOException {
        for (Iterator<HeaderSection.Entry> it = headers().entries(); it
                .hasNext();) {
            HeaderSection.Entry entry = it.next();
            if (entry.source == null) {
                String fieldAsString = entry.parsedField.generate();
                out.write(CharsetUtil.toAsciiBytes(fieldAsString));
            } else {
                out.write(toAsciiBytes(entry.source.originalSpelling));
            }
        }
        out.write(toAsciiBytes(map.separator));

        try (InputStream bodyInputStream = source.getInputStream()) {
            bodyInputStream.skip(map.bodyRange.start);
            StreamCopier.writeInputStreamIntoOutputStream(bodyInputStream, out);
        }
    }

    /**
     * Releases system resources associated with this object.
     */
    @Override
    public void close() {
        if (parser != null)
            parser.close();
        if (source != null)
            source.close();
        if (resultFile != null)
            resultFile.close();
    }
}
