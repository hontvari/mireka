package mireka.server;

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
        return new FilterChainMessageHandler(filterInstances, mailTransaction);
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
