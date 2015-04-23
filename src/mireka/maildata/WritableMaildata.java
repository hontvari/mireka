package mireka.maildata;

import static mireka.util.CharsetUtil.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.util.Iterator;

import mireka.MailData;
import mireka.util.StreamCopier;

public class WritableMaildata implements MailData {
    private MailData source;

    /**
     * Null if the mail is not yet parsed.
     */
    private MaildataParser.Result sourceMap;

    /**
     * It is initialized on demand, null if it is not yet initialized.
     */
    private HeaderSection headerSection;

    public WritableMaildata(MailData source) {
        this.source = source;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (isUpdated())
            return createUpdatedInputStream();
        else
            return source.getInputStream();
    }

    private InputStream createUpdatedInputStream() throws IOException {
        ByteArrayOutputStream arrayOutputStream =
                new ByteArrayOutputStream(8192);
        for (Iterator<HeaderSection.Entry> it = getHeaders().entries(); it
                .hasNext();) {
            HeaderSection.Entry entry = it.next();
            if (entry.source == null) {
                entry.parsedField.writeGenerated(arrayOutputStream);
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

    @Override
    public void writeTo(OutputStream out) throws IOException {
        StreamCopier.writeInputStreamIntoOutputStream(getInputStream(), out);

    }

    @Override
    public void dispose() {
        source.dispose();
    }

    public HeaderSection getHeaders() throws IOException {
        if (headerSection == null) {
            if (sourceMap == null) {
                sourceMap = new MaildataParser(getInputStream()).parse();
            }
            headerSection = sourceMap.headerSection;

        }
        return headerSection;
    }

    /**
     * Returns true if some part of this mail data has been updated, indicating
     * that the source MailData does not reflect the current state.
     */
    private boolean isUpdated() {
        return headerSection != null ? headerSection.isUpdated : false;
    }

}
