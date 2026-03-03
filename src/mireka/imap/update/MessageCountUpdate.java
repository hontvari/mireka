package mireka.imap.update;

import java.io.IOException;
import java.util.Objects;

import mireka.imap.Session;
import mireka.imap.parser.Generator;

/**
 * Represents an unsolicited notification telling that the mailbox has one or more new messages.
 */
public class MessageCountUpdate implements Update {
    public final long count;

    public MessageCountUpdate(long count) {
        this.count = count;
    }

    @Override
    public void send(Generator out, Session session) throws IOException {
        out.respondExists(count);
    }

    @Override
    public int hashCode() {
        return Objects.hash(MessageCountUpdate.class.hashCode());
    }

    /**
     * There is no need to send older instances.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        return getClass() == obj.getClass();
    }

}
