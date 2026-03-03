package mireka.maildata;

import java.io.OutputStream;
import java.util.List;

import mireka.maildata.type.MediaType;

/**
 * A body which consists of one or more entities, this type of body is handled by the MIME processor
 * directly.
 */
public class MultipartBody extends Body {
    public List<Entity> parts;

    public MultipartBody(MediaType kind, Entity owner) {
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
