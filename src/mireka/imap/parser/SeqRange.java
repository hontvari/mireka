package mireka.imap.parser;

/**
 * Normalized means that neither begin nor end contains asterisk, it is not a saved result
 * placeholder, and begin &lt;= end.
 */
public class SeqRange {
    /**
     * -1 means '*', that is the highest id of the mailbox
     */
    public long begin;
    /**
     * -1 means '*' that is the highest id of the mailbox
     */
    public long end;
    /**
     * If true this range is a placeholder for the result of the last seqence list producing
     * command.
     */
    public boolean lastResult;

    public SeqRange() {
    }

    public SeqRange(long id) {
        this.begin = id;
        this.end = id;
    }

    @Override
    public String toString() {
        if (lastResult)
            return "$";
        else if (begin == end) {
            return limitToString(begin);
        } else {
            return limitToString(begin) + ":" + limitToString(end);
        }
    }

    private String limitToString(long n) {
        if (n == -1)
            return "*";
        else
            return String.valueOf(n);
    }

    public boolean isNumber() {
        if (!isNormalized())
            throw new IllegalStateException();
        return begin == end;
    }

    private boolean isNormalized() {
        return !lastResult && begin != -1 && end != -1;
    }

}
