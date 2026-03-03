package mireka.maildata.io;

public class Range {
    public final long start;
    public final long length;

    public Range(long start, long length) {
        if (start < 0 || length < 0)
            throw new IllegalArgumentException();
        this.start = start;
        this.length = length;
    }

    /**
     * Creates a new range from the supplied interval.
     * 
     * @param start inclusive
     * @param end exclusive
     */
    public static Range ofFromTo(long start, long end) {
        if (end < start)
            throw new IllegalArgumentException();
        return new Range(start, end - start);
    }

    public static Range of(long length) {
        if (length < 0)
            throw new IllegalArgumentException();
        return new Range(0, length);
    }

    public static Range of(long start, long length) {
        if (start < 0 || length < 0)
            throw new IllegalArgumentException();
        return new Range(start, length);
    }
}