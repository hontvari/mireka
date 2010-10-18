package mireka.filter.misc;

import java.util.Date;

public interface TrafficSummaryMBean {
    Date getSince();

    int getMailTransactions();

    int getRcptCommands();

    int getDataCommands();

    int getAcceptedMessages();

    int getMessageRecipients();

}
