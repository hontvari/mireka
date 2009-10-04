package mireka.filter;

/**
 * 
 * Implementing classes are active parts of a filter chain. Their methods
 * explicitly call the corresponding method of the next filter. In this way they
 * are able to get information about the results of methods of the following
 * filters. This design is similar to Servlet Filters.
 * 
 * @see <a
 *      href="http://java.sun.com/products/servlet/2.3/javadoc/javax/servlet/Filter.html">ServletFilter
 *      in the Servlet API</a>
 * 
 */
public interface Filter extends FilterBase {
    /**
     * an implementation must store the supplied view to the next filter in the
     * chain. All methods of the implementing class which has a corresponding
     * method in {@link FilterChain} must call the latter method.
     */
    void setChain(FilterChain chain);

}
