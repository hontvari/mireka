package mireka.imap.server;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LoggingInputStream extends FilterInputStream {
    private final ProtocolLogger protocolLogger;

    protected LoggingInputStream(InputStream in, ProtocolLogger protocolLogger) {
        super(in);
        this.protocolLogger = protocolLogger;
    }

    @Override
    public int read() throws IOException {
        int b = super.read();
        protocolLogger.logClientStream(b);
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int length = super.read(b, off, len);
        if (length != -1)
            protocolLogger.logClientStream(b, off, length);
        return length;
    }
}
