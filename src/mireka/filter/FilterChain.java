package mireka.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * The list of filters. During a mail transaction the first filter will call the
 * second, the second will call the third and so on.
 */
public class FilterChain {
    private final List<Filter> filters = new ArrayList<Filter>();

    public void setFilters(List<Filter> filters) {
        this.filters.clear();
        this.filters.addAll(filters);
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public void addFilter(Filter filter) {
        filters.add(filter);
    }

}
