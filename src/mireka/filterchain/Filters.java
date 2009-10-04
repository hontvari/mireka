package mireka.filterchain;

import java.util.ArrayList;
import java.util.List;

import mireka.filter.Filter;
import mireka.filter.FilterType;
import mireka.filter.MailTransaction;

public class Filters {
    private final List<FilterType> filters = new ArrayList<FilterType>();

    public void addFilter(FilterType filter) {
        filters.add(filter);
    }

    public FilterInstances createInstanceChain(
            MailTransaction mailTransaction) {
        FilterInstances instanceChain = new FilterInstances(mailTransaction);
        for (FilterType filterType : filters) {
            Filter filter = filterType.createInstance(mailTransaction);
            instanceChain.add(filter);
        }
        instanceChain.init();
        return instanceChain;
    }
}
