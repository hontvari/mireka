package mireka.transmission.immediate.host;

import java.io.IOException;
import java.io.OutputStream;

import org.subethamail.smtp.client.SmartClient;

/**
 * An {@link OutputStream} which redirects all writes to a {@link SmartClient}.
 */
class SmartClientOutputStreamAdapter extends OutputStream {

    private final SmartClient client;
    private byte[] buffer = new byte[1];

    public SmartClientOutputStreamAdapter(SmartClient client) {
        this.client = client;
    }

    @Override
    public void write(int b) throws IOException {
        buffer[0] = (byte) b;
        client.dataWrite(buffer, 1);
    }

    @Override
    public void write(byte[] b) throws IOException {
        client.dataWrite(b, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (off == 0) {
            client.dataWrite(b, len);
        } else {
            if (len > buffer.length) {
                buffer = new byte[len];
            }
            System.arraycopy(b, off, buffer, 0, len);
            client.dataWrite(buffer, len);
        }
    }

}