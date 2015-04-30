package mireka.maildata;

import static mireka.maildata.FieldDef.*;
import static mireka.maildata.MediaType.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import mireka.maildata.field.Cc;
import mireka.maildata.field.ContentType;
import mireka.maildata.field.From;
import mireka.maildata.field.ReplyTo;
import mireka.maildata.field.UnstructuredField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SmartHeaderSection represents the semantic content of the header section of
 * the mail data, for example it can return or set the complete lists of
 * authors. This is in contrast to the {@link HeaderSection} which deals with
 * individual header fields.
 * 
 * Idea: Functionality of this class may be added to {@link WritableMaildata}.
 */
public class SmartHeaderSection {
    private final Logger logger = LoggerFactory
            .getLogger(SmartHeaderSection.class);

    public HeaderSection store;

    public SmartHeaderSection(HeaderSection store) {
        this.store = store;
    }

    public List<Address> getFromAddresses() throws ParseException {
        List<Address> result = new ArrayList<>();
        From f = store.get(FROM);
        if (f == null)
            return result;
        result.addAll(f.addressList);
        return result;
    }

    public void setFromAddresses(List<Address> addresses) {
        From f = new From();
        f.addressList.addAll(addresses);
        store.put(f);
    }

    public List<Address> getReplyToAddresses() throws ParseException {
        List<Address> result = new ArrayList<>();
        ReplyTo f = store.get(REPLY_TO);
        if (f == null)
            return result;
        result.addAll(f.addressList);
        return result;
    }

    public void setReplyToAddresses(List<Address> addresses) {
        ReplyTo f = new ReplyTo();
        f.addressList.addAll(addresses);
        store.put(f);
    }

    public List<Address> getCcAddresses() throws ParseException {
        List<Address> result = new ArrayList<>();
        List<Cc> fieldList = store.getAll(CC);
        for (Cc cc : fieldList) {
            result.addAll(cc.addressList);
        }
        return result;
    }

    public void setCcAddresses(List<Address> addresses) {
        Cc f = new Cc();
        f.addressList.addAll(addresses);
        store.put(f);
    }

    public MediaType getMediaType() {
        try {
            ContentType contentType = store.get(CONTENT_TYPE);
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
            UnstructuredField field = store.get(SUBJECT);
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
            store.remove(SUBJECT);
        UnstructuredField field = new UnstructuredField();
        // Conventionally there is a space before the actual text, so the
        // heading looks better, even though this is somewhat wrong, because the
        // space becomes part of the semanantic value.
        field.body = " " + subject;
        store.put(field);
    }

}
