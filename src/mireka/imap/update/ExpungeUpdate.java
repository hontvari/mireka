package mireka.imap.update;

import java.io.IOException;

import mireka.imap.Session;
import mireka.imap.parser.Generator;

public class ExpungeUpdate implements Update {
    public long seq;
    public UnilateralResponseOption option;

    @Override
    public void send(Generator out, Session session) throws IOException {
        if (session == option.notForSession)
            return;
        out.respondExpunge(seq);
    }

}
