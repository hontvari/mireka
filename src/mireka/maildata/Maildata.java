package mireka.maildata;

import static mireka.maildata.FieldDef.*;
import static mireka.maildata.MediaType.*;
import static mireka.util.CharsetUtil.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mireka.maildata.field.Cc;
import mireka.maildata.field.ContentType;
import mireka.maildata.field.From;
import mireka.maildata.field.ReplyTo;
import mireka.maildata.field.UnstructuredField;
import mireka.maildata.io.DeferredFile;
import mireka.maildata.io.MaildataFile;
import mireka.maildata.io.MaildataFileInputStream;
import mireka.maildata.io.TmpMaildataFile;
import mireka.maildata.parser.MaildataParser;
import mireka.util.CharsetUtil;
import mireka.util.StreamCopier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maildata represents a message sent between computer users in the format of
 * Internet Message Format. An electronic mail consists of an envelope and the
 * content. This class represents the content.
 * 
 * Header section related functions in this class represents the high level,
 * semantic content of the header section compared to the lower level header
 * fields. For example it can return or set the complete lists of authors. This
 * is in contrast to the {@link HeaderSection}, returned by {@link #header()}
 * which deals with individual header fields.
 * 
 * @see <a href="https://tools.ietf.org/html/rfc5322">RFC 5322 - Internet
 *      Message Format</a>
 */
public class Maildata implements AutoCloseable {
    private final Logger logger = LoggerFactory.getLogger(Maildata.class);

    private final MaildataFile sourceFile;

    /**
     * Null if the mail is not yet parsed.
     */
    private MaildataParser.MaildataMap sourceFileMap;

    /**
     * It is initialized on demand, null if it is not yet initialized.
     */
    private HeaderSection headerSection;

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

    public HeaderSection headers() {
        if (headerSection == null) {
            if (sourceFileMap == null) {
                try (MaildataFileInputStream in = sourceFile.getInputStream()) {
                    sourceFileMap = new MaildataParser(in).parse();
                }
            }
            headerSection = sourceFileMap.headerSection;

        }
        return headerSection;
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

    public List<Address> getFromAddresses() throws ParseException {
        List<Address> result = new ArrayList<>();
        From f = headers().get(FROM);
        if (f == null)
            return result;
        result.addAll(f.addressList);
        return result;
    }

    public void setFromAddresses(List<Address> addresses) {
        From f = new From();
        f.addressList.addAll(addresses);
        headers().put(f);
    }

    public List<Address> getReplyToAddresses() throws ParseException {
        List<Address> result = new ArrayList<>();
        ReplyTo f = headers().get(REPLY_TO);
        if (f == null)
            return result;
        result.addAll(f.addressList);
        return result;
    }

    public void setReplyToAddresses(List<Address> addresses) {
        ReplyTo f = new ReplyTo();
        f.addressList.addAll(addresses);
        headers().put(f);
    }

    public List<Address> getCcAddresses() throws ParseException {
        List<Address> result = new ArrayList<>();
        List<Cc> fieldList = headers().getAll(CC);
        for (Cc cc : fieldList) {
            result.addAll(cc.addressList);
        }
        return result;
    }

    public void setCcAddresses(List<Address> addresses) {
        Cc f = new Cc();
        f.addressList.addAll(addresses);
        headers().put(f);
    }

    public MediaType getMediaType() {
        try {
            ContentType contentType = headers().get(CONTENT_TYPE);
            return contentType != null ? contentType.mediaType
                    : TEXT_PLAIN_US_ASCII;
        } catch (ParseException e) {
            logger.debug("Invalid Content-Type, using default.", e);
            return TEXT_PLAIN_US_ASCII;
        }
    }

    /**
     * Returns the subject of the mail, if there was no subject, then it returns
     * an empty string.
     */
    public String getSubject() {
        try {
            UnstructuredField field = headers().get(SUBJECT);
            if (field == null)
                return "";
            return field.body.trim();
        } catch (ParseException e) {
            // Unstructured field parser does not throw ParseException
            throw new RuntimeException("Assertion failed");
        }
    }

    public void setSubject(String s) {
        String subject = s.trim();
        if (subject.isEmpty())
            headers().remove(SUBJECT);
        UnstructuredField field = new UnstructuredField();
        // Conventionally there is a space before the actual text, so the
        // heading looks better, even though this is somewhat wrong, because the
        // space becomes part of the semanantic value.
        field.body = " " + subject;
        headers().put(field);
    }
}
