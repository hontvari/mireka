package mireka.maildata.io;

/**
 * Combination of {@link MaildataSource} and {@link Range} within that source.
 */
public class Subsource {
    public final MaildataSource source;
    public final Range range;

    public Subsource(MaildataSource source, Range range) {
        this.source = source;
        this.range = range;
    }

    public MaildataInputStream getInputStream() {
        return source.getInputStream(range);
    }

    public Subsource sub(Range range) {
        return new Subsource(source, range);
    }

    public Subsource subSkipping(long length) {
        return new Subsource(source, range);
    }

    public Subsource left(long length) {
        return new Subsource(source, Range.of(range.start, length));
    }

    public Subsource to(long end) {
        return new Subsource(source, Range.ofFromTo(range.start, end));
    }

    public static Subsource entireOf(MaildataSource source) {
        return new Subsource(source, Range.of(source.length()));
    }
}
