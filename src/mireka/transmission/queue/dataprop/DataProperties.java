package mireka.transmission.queue.dataprop;

import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public class DataProperties extends Properties {
    private static final long serialVersionUID = -3219839598807297855L;
    private static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public void setString(String key, String value) {
        if (value == null)
            return;
        setProperty(key, value);
    }

    public String getString(String key) {
        return getProperty(key);
    }

    public void setDate(String key, Date value) {
        if (value == null)
            return;
        SimpleDateFormat format =
                new SimpleDateFormat(ISO_DATE_FORMAT, Locale.US);
        String s = format.format(value);
        setProperty(key, s);
    }

    public Date getDate(String key) {
        String s = getProperty(key);
        if (s == null)
            return null;
        SimpleDateFormat format =
                new SimpleDateFormat(ISO_DATE_FORMAT, Locale.US);
        try {
            return format.parse(s);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date property", e);
        }
    }

    public void setInt(String key, int value) {
        if (value == 0)
            return;
        String s = Integer.toString(value);
        setProperty(key, s);
    }

    public int getInt(String key) {
        String s = getProperty(key);
        if (s == null)
            return 0;
        Integer value = Integer.valueOf(s);
        return value.intValue();
    }

    public void setList(String key, List<?> value) {
        if (value.size() == 0)
            return;
        String valueString = new ListFormatter(value).format();
        setProperty(key, valueString);
    }

    public <T> List<T> getList(String key,
            StringToElementConverter<T> stringToElementConverter) {
        String s = getProperty(key);
        if (s == null)
            return Collections.emptyList();
        try {
            return new ListParser<T>(s, stringToElementConverter).parse();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public void setInetAddress(String key, InetAddress value) {
        if (value == null)
            return;
        String valueString = value.toString();
        setProperty(key, valueString);
    }

    public InetAddress getInetAddress(String key) {
        String s = getProperty(key);
        if (s == null)
            return null;
        return new InetAddressParser(s).parse();
    }
}
