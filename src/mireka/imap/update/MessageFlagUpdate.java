package mireka.imap.update;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import mireka.imap.CiString;
import mireka.imap.MessageFlagSet;
import mireka.imap.ParenthesizedList;
import mireka.imap.Session;
import mireka.imap.parser.Generator;

/**
 * Represents an unsolicited notification telling that the message flags of a mail in the selected
 * mailbox has been changed. For example the \\Seen flag is set.
 */
public class MessageFlagUpdate implements Update {

    public long uid;
    public long seq;
    public MessageFlagSet flags;
    public UnilateralResponseOption option;

    @Override
    public void send(Generator out, Session session) throws IOException {
        if (session == option.notForSession)
            return;
        ParenthesizedList data = new ParenthesizedList();
        List<String> flagList = new ArrayList<>();
        for (CiString flag : flags) {
            flagList.add(flag.toString());
        }
        data.addNamedKeywordList("FLAGS", flagList);
        data.addNamedAstring("UID", Long.toUnsignedString(uid));
        out.respondFetch(seq, data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MessageFlagUpdate other = (MessageFlagUpdate) obj;
        return uid == other.uid;
    }
}
