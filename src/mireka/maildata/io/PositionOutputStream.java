package mireka.maildata.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PositionOutputStream extends FilterOutputStream {
    /**
     * The {@link MaildataSource} corresponding to the underlying stream, which will be used to read
     * back this message or part of this message.
     */
    private MaildataSource destination;

    /**
     * Position of the next byte which will be read.
     */
    public long position = 0;

    /**
     * @param source the source which is used by the {@link #copy(Range)} functions.
     * @param out the underlying stream
     */
    public PositionOutputStream(MaildataSource destination, OutputStream out) {
        super(out);
        this.destination = destination;
    }

    @Override
    public void write(int b) throws IOException {
        super.write(b);
        position++;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
        position += len;
    }

    public void copy(Subsource sourceRange) throws MaildataReadException, IOException {
        sourceRange.getInputStream().transferTo(this);
    }

    public Subsource subsource(long start) {
        return new Subsource(destination, Range.ofFromTo(start, position));
    }
}
