package mireka.maildata;

import static mireka.util.CharsetUtil.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.util.Iterator;

import mireka.maildata.parser.MaildataParser;
import mireka.smtp.server.DeferredFileMaildataFile;
import mireka.util.CharsetUtil;
import mireka.util.StreamCopier;

import org.subethamail.smtp.io.DeferredFileOutputStream;

/**
 * Maildata represents a message sent between computer users in the format of
 * Internet Message Format. An electronic mail consists of an envelope and the
 * content. This class represents the content.
 * 
 * @see <a href="https://tools.ietf.org/html/rfc5322">RFC 5322 - Internet
 *      Message Format</a>
 */
public class Maildata {
    private MaildataFile source;

    /**
     * Null if the mail is not yet parsed.
     */
    private MaildataParser.MaildataMap sourceMap;

    /**
     * It is initialized on demand, null if it is not yet initialized.
     */
    private HeaderSection headerSection;

    /**
     * It is initialized on demand, null if it is not yet initialized.
     */
    private SmartHeaderSection smartHeaderSection;

    /**
     * Initializes a new Maildata object with the supplied mail data byte
     * stream.
     * 
     * @param source
     *            the MaildataFile which contains the byte representation of the
     *            Mail Data. This object stores a reference to this MaildataFile
     *            object, but closing it is still the responsibility of the
     *            caller.
     */
    public Maildata(MaildataFile source) {
        this.source = source;
    }

    public MaildataFile toMailData() throws IOException {
        if (isUpdated()) {
            DeferredFileOutputStream out =
                    new DeferredFileOutputStream(0x10000);
            try {
                writeTo(out);
                return new DeferredFileMaildataFile(out);
            } catch (IOException e) {
                out.close();
                throw e;
            }
        } else {
            return source;
        }
    }

    private InputStream createUpdatedInputStream() throws IOException {
        ByteArrayOutputStream arrayOutputStream =
                new ByteArrayOutputStream(8192);
        for (Iterator<HeaderSection.Entry> it = getHeaders().entries(); it
                .hasNext();) {
            HeaderSection.Entry entry = it.next();
            if (entry.source == null) {
                String fieldAsString = entry.parsedField.generate();
                arrayOutputStream
                        .write(CharsetUtil.toAsciiBytes(fieldAsString));
            } else {
                arrayOutputStream
                        .write(toAsciiBytes(entry.source.originalSpelling));
            }
        }
        arrayOutputStream.write(toAsciiBytes(sourceMap.separator));
        ByteArrayInputStream headerInputStream =
                new ByteArrayInputStream(arrayOutputStream.toByteArray());
        InputStream bodyInputStream = source.getInputStream();
        try {
            bodyInputStream.skip(sourceMap.bodyPosition);
        } catch (IOException e) {
            bodyInputStream.close();
            throw e;
        }
        return new SequenceInputStream(headerInputStream, bodyInputStream);
    }

    private void writeTo(OutputStream out) throws IOException {
        StreamCopier.writeInputStreamIntoOutputStream(
                createUpdatedInputStream(), out);

    }

    public HeaderSection getHeaders() throws IOException {
        if (headerSection == null) {
            if (sourceMap == null) {
                try (InputStream in = source.getInputStream()) {
                    sourceMap = new MaildataParser(in).parse();
                }
            }
            headerSection = sourceMap.headerSection;

        }
        return headerSection;
    }

    public SmartHeaderSection header() throws IOException {
        if (smartHeaderSection == null) {
            smartHeaderSection = new SmartHeaderSection(getHeaders());
        }
        return smartHeaderSection;
    }

    /**
     * Returns true if some part of this mail data has been updated, indicating
     * that the source MailData does not reflect the current state.
     */
    private boolean isUpdated() {
        return headerSection != null ? headerSection.isUpdated : false;
    }

}
