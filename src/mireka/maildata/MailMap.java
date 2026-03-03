package mireka.maildata;

import mireka.maildata.io.Range;

public class MailMap {
    /**
     * It may be an empty string, although that is semantically invalid. Null if the maildata
     * represents an existing mail but the header section is not parsed yet.
     */
    public HeaderSection headerSection;
    public Range headerRange;
    /**
     * Null, if no separator presents, which also means that there is no
     * body.
     */
    public String separator;
    public Range separatorRange;
    /**
     * null means that there is no body, this happens when no separator is
     * found.
     */
    public Range bodyRange;
}