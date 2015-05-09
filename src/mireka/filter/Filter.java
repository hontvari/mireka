package mireka.filter;

/**
 * A Filter listens to an SMTP mail transaction (from the MAIL command to the
 * DATA command), and it may verify and process the mail envelope and the mail
 * data, including performing the final delivery.
 * 
 * A Filter itself is just a configuration object and a factory of a
 * FilterSession descendant class. The latter object does the real work.
 * 
 * Most filters are interested in only one point of the mail transaction. These
 * filters should extend the {@link StatelessFilter} class. This result in a
 * slightly more compact code.
 * 
 * Filters which interact with the mail transaction in a more complex way should
 * directly implement this interface and use a custom <code>FilterSession</code>
 * descendant class.
 */
public interface Filter {
    /**
     * Returns a new <code>FilterSession</code> descendant object which will
     * follow a specific mail transaction. The initialization of the object
     * (calling {@link FilterSession#setNextLink(FilterSession)} and so on) is
     * not the responsibility of this class.
     */
    FilterSession createSession();
}
