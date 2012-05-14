package mireka.smtp.server;

import mireka.filterchain.FilterInstances;
import mireka.filterchain.Filters;

import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;

public class MessageHandlerFactoryImpl implements MessageHandlerFactory {

    private Filters filters;

    @Override
    public MessageHandler create(MessageContext ctx) {
        MailTransactionImpl mailTransaction = new MailTransactionImpl(ctx);
        FilterInstances filterInstances =
                filters.createInstanceChain(mailTransaction);
        FilterChainMessageHandler filterChainMessageHandler =
                new FilterChainMessageHandler(filterInstances, mailTransaction);
        return new ErrorHandlerMessageHandler(filterChainMessageHandler);
    }

    /**
     * @category GETSET
     */
    public Filters getFilters() {
        return filters;
    }

    /**
     * @category GETSET
     */
    public void setFilters(Filters filters) {
        this.filters = filters;
    }
}
