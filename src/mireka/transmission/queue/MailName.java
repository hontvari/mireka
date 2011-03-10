package mireka.transmission.queue;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class MailName implements Comparable<MailName> {
    private static final String MESSAGE_CONTENT_DOT_EXTENSION = ".eml";
    public static final String MESSAGE_ENVELOPE_DOT_EXTENSION = ".properties";
    private static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH-mm-ss.SSSZ";
    private static final int ISO_DATE_LENGTH = 28;
    /**
     * not includes extension
     */
    public final String baseFileName;
    /**
     * milliseconds from epoch
     */
    public final long scheduleDate;
    public final int sequenceNumber;

    public MailName(String fileName) {
        ParsePosition pos = new ParsePosition(0);
        Date date = parseDate(fileName, pos);
        this.scheduleDate = date.getTime();

        if (parsePositinIsAtEndOfString(pos, fileName)
                || charOfStringAtParsePosition(fileName, pos) != '_') {
            this.sequenceNumber = 0;
        } else {
            incrementParsePosition(pos);
            this.sequenceNumber = parseSequenceNumber(fileName, pos);
        }
        this.baseFileName = fileName.substring(0, pos.getIndex());
    }

    public MailName(long scheduleDate, int sequenceNumber) {
        this.scheduleDate = scheduleDate;
        this.sequenceNumber = sequenceNumber;
        SimpleDateFormat format =
                new SimpleDateFormat(ISO_DATE_FORMAT, Locale.US);
        String baseName = format.format(scheduleDate);
        if (sequenceNumber != 0)
            baseName += "_" + sequenceNumber;
        this.baseFileName = baseName;
    }

    private Date parseDate(String baseFileName, ParsePosition parsePosition) {
        String dateString =
                baseFileName.substring(parsePosition.getIndex(), parsePosition
                        .getIndex()
                        + ISO_DATE_LENGTH);
        SimpleDateFormat format =
                new SimpleDateFormat(ISO_DATE_FORMAT, Locale.US);
        Date date;
        try {
            date = format.parse(dateString);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
        parsePosition.setIndex(parsePosition.getIndex() + ISO_DATE_LENGTH);
        return date;
    }

    private boolean parsePositinIsAtEndOfString(ParsePosition parsePosition,
            String s) {
        return parsePosition.getIndex() == s.length();
    }

    private char charOfStringAtParsePosition(String s,
            ParsePosition parsePosition) {
        return s.charAt(parsePosition.getIndex());
    }

    private void incrementParsePosition(ParsePosition parsePosition) {
        parsePosition.setIndex(parsePosition.getIndex() + 1);
    }

    private int parseSequenceNumber(String s, ParsePosition parsePosition) {
        StringBuilder sequenceString = new StringBuilder();
        int i = parsePosition.getIndex();
        char ch;
        while (Character.isDigit(ch = s.charAt(i++))) {
            sequenceString.append(ch);
        }
        return Integer.parseInt(sequenceString.toString());
    }

    public String envelopeFileName() {
        return baseFileName + MESSAGE_ENVELOPE_DOT_EXTENSION;
    }

    public String contentFileName() {
        return baseFileName + MESSAGE_CONTENT_DOT_EXTENSION;
    }

    @Override
    public int compareTo(MailName o) {
        int relation = Long.signum(scheduleDate - o.scheduleDate);
        if (relation != 0)
            return relation;
        return sequenceNumber - o.sequenceNumber;
    }

    @Override
    public String toString() {
        return baseFileName;
    }
}