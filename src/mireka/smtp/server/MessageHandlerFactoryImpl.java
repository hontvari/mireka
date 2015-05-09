package mireka.smtp.server;

import mireka.filter.FilterChain;

import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;

/**
 * MessageHandlerFactoryImpl is called by SubEthaSMTP to handle a mail
 * transaction; it returns a {@link FilterChainMessageHandler}, which sends all
 * transaction events to the filter chain, wrapped in an
 * {@link ErrorHandlerMessageHandler} which handles RuntimeExceptions gracefully
 * from the viewpoint of the client.
 */
public class MessageHandlerFactoryImpl implements MessageHandlerFactory {

    private FilterChain filters;

    @Override
    public MessageHandler create(MessageContext ctx) {
        FilterChainMessageHandler filterChainMessageHandler =
                new FilterChainMessageHandler(ctx, filters.getFilters());
        return new ErrorHandlerMessageHandler(filterChainMessageHandler);
    }

    /**
     * @x.category GETSET
     */
    public FilterChain getFilters() {
        return filters;
    }

    /**
     * @x.category GETSET
     */
    public void setFilters(FilterChain filters) {
        this.filters = filters;
    }
}
