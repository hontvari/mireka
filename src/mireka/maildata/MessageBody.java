package mireka.maildata;

import java.io.OutputStream;

import mireka.maildata.type.MediaType;

/**
 * A body which consists of another message.
 */
public class MessageBody extends Body {
    Entity content;

    public MessageBody(MediaType kind, Entity owner) {
        super(kind, owner);
    }

    @Override
    public boolean isUpdated() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void writeTo(OutputStream out) {
        // TODO Auto-generated method stub

    }
}
