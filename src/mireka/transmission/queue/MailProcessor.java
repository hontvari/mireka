package mireka.transmission.queue;

import mireka.transmission.LocalMailSystemException;

public interface MailProcessor {

    void run() throws LocalMailSystemException;

}
