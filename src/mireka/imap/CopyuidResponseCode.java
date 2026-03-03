package mireka.imap;

import java.io.IOException;

import mireka.imap.parser.Generator;

public class CopyuidResponseCode implements ResponseCode {
    public long uidvalidity;
    public SequenceSet srcSeq;
    public SequenceSet dstSeq;

    public CopyuidResponseCode(long uidvalidity, SequenceSet srcSeq, SequenceSet dstSeq) {
        this.uidvalidity = uidvalidity;
        this.srcSeq = srcSeq;
        this.dstSeq = dstSeq;
    }

    @Override
    public void generate(Generator out) throws IOException {
        out.writeResponseCodeCopy(this);
    }

}
