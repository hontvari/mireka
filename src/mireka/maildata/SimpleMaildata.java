package mireka.maildata;

import static mireka.maildata.parser.Kind.*;
import static mireka.maildata.type.MediaParameterKind.*;
import static mireka.maildata.type.MediaType.TEXT_PLAIN_US_ASCII;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mireka.maildata.field.AddressListField;
import mireka.maildata.field.ContentType;
import mireka.maildata.field.DateField;
import mireka.maildata.field.MessageIdField;
import mireka.maildata.field.MimeVersion;
import mireka.maildata.field.UnstructuredField;
import mireka.maildata.parser.Kind;
import mireka.maildata.type.AddrSpec;
import mireka.maildata.type.Address;
import mireka.maildata.type.DomainPart;
import mireka.maildata.type.Mailbox;
import mireka.maildata.type.MediaParameter;
import mireka.maildata.type.MediaType;
import mireka.maildata.type.SubMediaType;
import mireka.smtp.address.ReversePath;

/**
 * Contains high level maildata functionality instead of manipulating fields, for example it can set
 * the From address in one function call instead of creating and adding a From header field.
 * 
 * For a new generated mail at least {@link #setOriginationDate} and {@link #setFromAddresses} must
 * be set, and {@link #setMessageId(DomainPart)} should be set.
 */
public class SimpleMaildata {
    private static final Random random = new Random();
    private final Logger logger = LoggerFactory.getLogger(SimpleMaildata.class);
    public Entity d;

    public SimpleMaildata(Entity entity) {
        this.d = entity;
    }

    private HeaderSection headers() {
        return d.headers();
    }

    public List<Address> getCcAddresses() throws ParseException {
        List<Address> result = new ArrayList<>();
        List<AddressListField> fieldList = headers().getAll(CC, AddressListField.class);
        for (AddressListField cc : fieldList) {
            result.addAll(cc.addressList);
        }
        return result;
    }

    public void setCcAddresses(List<Address> addresses) {
        AddressListField f = new AddressListField(CC);
        f.addressList.addAll(addresses);
        headers().put(f);
    }

    public void setContentType(MediaType mediaType) {
        ContentType f = new ContentType();
        f.mediaType = mediaType;
        headers().put(f);
    }

    public void setContentType(String mediaType) throws ParseException {
        setContentType(MediaType.parse(mediaType));
    }

    public boolean isAsciiOnly(String text) {
        return text.chars().allMatch(v -> v <= 0x7F);
    }

    public void setFixedText(String text) {
        setText(text, List.of(new MediaParameter(Format, "fixed")));
    }

    public void setText(String text, List<MediaParameter> parameters) {
        MediaType type = new MediaType(SubMediaType.TextPlain);
        String charset = isAsciiOnly(text) ? "US-ASCII" : "UTF-8";
        type.setParameter(Charset, charset);
        type.setParameters(parameters);

    }

    public List<Address> getFromAddresses() throws ParseException {
        List<Address> result = new ArrayList<>();
        AddressListField f = headers().get(FROM, AddressListField.class);
        if (f == null)
            return result;
        result.addAll(f.addressList);
        return result;
    }

    public void setFromAddresses(List<Address> addresses) {
        AddressListField f = new AddressListField(FROM);
        f.addressList.addAll(addresses);
        headers().put(f);
    }

    public void setFrom(String displayName, String address) throws ParseException {
        Mailbox mailbox = new Mailbox();
        mailbox.displayName = displayName;
        mailbox.addrSpec = AddrSpec.fromString(address);
        setFrom(mailbox);
    }

    public void setFrom(Address address) {
        AddressListField f = new AddressListField(FROM);
        f.addressList.add(address);
        headers().put(f);
    }

    public MediaType getMediaType() {
        try {
            ContentType contentType = headers().get(CONTENT_TYPE, ContentType.class);
            return contentType != null ? contentType.mediaType : TEXT_PLAIN_US_ASCII;
        } catch (ParseException e) {
            logger.debug("Invalid Content-Type, using default.", e);
            return TEXT_PLAIN_US_ASCII;
        }
    }

    public void setMessageId(DomainPart domain) {
        MessageIdField f = new MessageIdField(Kind.MESSAGE_ID);
        f.msgId = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss.SSS.")
                .format(LocalDateTime.now()) + random.nextInt(1_000_000) + "@" + domain.generate();
        headers().put(f);
    }

    public boolean isMime_1_0() throws ParseException {
        for (MimeVersion f : headers().getAll(Kind.MIME_VERSION, MimeVersion.class)) {
            if (f.major == 1 && f.minor == 0)
                return true;
        }
        return false;
    }

    public void setMimeVersion() {
        MimeVersion f = new MimeVersion();
        headers().put(f);
    }

    public void setOriginationDate(ZonedDateTime date) {
        DateField f = new DateField(ORIG_DATE);
        f.date = date;
        headers().put(f);
    }

    public void setOriginationDate(Instant date) {
        setOriginationDate(ZonedDateTime.ofInstant(date, ZoneId.systemDefault()));
    }

    public List<Address> getReplyToAddresses() throws ParseException {
        List<Address> result = new ArrayList<>();
        AddressListField f = headers().get(REPLY_TO, AddressListField.class);
        if (f == null)
            return result;
        result.addAll(f.addressList);
        return result;
    }

    public void setReplyToAddresses(List<Address> addresses) {
        AddressListField f = new AddressListField(REPLY_TO);
        f.addressList.addAll(addresses);
        headers().put(f);
    }

    /**
     * Returns the subject of the mail, if there was no subject, then it returns an empty string.
     */
    public String getSubject() {
        try {
            UnstructuredField field = headers().get(SUBJECT, UnstructuredField.class);
            if (field == null)
                return "";
            return field.body;
        } catch (ParseException e) {
            // Unstructured field parser does not throw ParseException
            throw new RuntimeException("Assertion failed");
        }
    }

    public void setSubject(String s) {
        String subject = s.trim();
        if (subject.isEmpty())
            headers().remove(SUBJECT);
        UnstructuredField field = new UnstructuredField(SUBJECT);
        field.setBody(subject);
        headers().put(field);
    }

    public List<Address> getToAddresses() throws ParseException {
        List<Address> result = new ArrayList<>();
        List<AddressListField> fieldList = headers().getAll(TO, AddressListField.class);
        for (AddressListField cc : fieldList) {
            result.addAll(cc.addressList);
        }
        return result;
    }

    public void setToAddresses(List<Address> addresses) {
        AddressListField f = new AddressListField(TO);
        f.addressList.addAll(addresses);
        headers().put(f);
    }

    public void setTo(Address address) {
        AddressListField f = new AddressListField(TO);
        f.addressList.add(address);
        headers().put(f);
    }

    public void prependReturnPath(ReversePath from) {
        // TODO Auto-generated method stub

    }

}
