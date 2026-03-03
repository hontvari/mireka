package mireka.imap.parser;

import java.io.IOException;
import java.io.InputStream;

import mireka.imap.Session;

public class Literal {
    private final Session session;
    private final InputStream in;

    long size;
    boolean synchronizing;

    public Literal(Session session) {
        this.session = session;
        this.in = session.connection.input;
    }

    public InputStream getLimitedInputStream() throws IOException {
        if (synchronizing) {
            session.connection.sendResponse("+ Ready for additional command text");
        }
        return new InputStream() {
            long position = 0;

            @Override
            public int read() throws IOException {
                if (position >= size)
                    return -1;
                position++;
                return in.read();
            }
        };
    }

    public void skip() throws IOException {
        if (!synchronizing) {
            InputStream stream = getLimitedInputStream();
            stream.skip(size);
        }
    }
}
