package mireka.maildata;

import java.io.OutputStream;

import mireka.maildata.type.MediaType;

/**
 * A body of an entity, which is opaque to MIME processors, for example the body of a text/plain
 * entity.
 */
public class DiscreteBody extends Body {

    public DiscreteBody(MediaType kind, Entity owner) {
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
