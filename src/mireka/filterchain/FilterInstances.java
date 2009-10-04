package mireka.filterchain;

import java.util.ArrayList;
import java.util.List;

import mireka.filter.Filter;
import mireka.filter.FilterBase;
import mireka.filter.FilterChain;
import mireka.filter.MailTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilterInstances {
    private final Logger logger =
            LoggerFactory.getLogger(FilterInstances.class);
    private final List<Filter> filters = new ArrayList<Filter>();
    private final MailTransaction mailTransaction;
    private FilterChain head;

    public FilterInstances(MailTransaction mailTransaction) {
        this.mailTransaction = mailTransaction;
    }

    public void add(Filter filter) {
        filters.add(filter);
    }

    public void init() {
        FilterChain nextLink = new ChainEnd(mailTransaction);
        for (int i = filters.size() - 1; i >= 0; i--) {
            Filter filter = filters.get(i);
            filter.setChain(nextLink);
            nextLink = new Link(filter, mailTransaction);
        }
        head = nextLink;
    }

    public FilterChain getHead() {
        return head;
    }

    /**
     * calls done method of all filters even if one or more fails.
     */
    public void done() {
        for (FilterBase filter : filters) {
            try {
                filter.done();
            } catch (RuntimeException e) {
                logger.error("Exception in done method of filter. "
                        + "done method of other filters will still run.", e);
            }
        }
    }
}
