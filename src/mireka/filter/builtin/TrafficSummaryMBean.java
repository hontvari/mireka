package mireka.filter.builtin;

import java.util.Date;

public interface TrafficSummaryMBean {
    Date getSince();

    int getMailTransactions();

    int getRcptCommands();

    int getDataCommands();

    int getAcceptedMessages();

    int getMessageRecipients();

}
