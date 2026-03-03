package mireka.imap.update;

import java.io.IOException;

import mireka.imap.Session;
import mireka.imap.parser.Generator;

/**
 * Represents an update about the status of a selected mailbox, which will be sent unilaterally at
 * the end of the next command.
 */
public interface Update {
    void send(Generator out, Session session) throws IOException;

    /**
     * It must correspond to the {@link #equals(Object)} function, see there.
     */
    @Override
    int hashCode();

    /**
     * An instance must be equal with another if a newer update makes the older update unnecessary.
     */
    @Override
    boolean equals(Object obj);
}
