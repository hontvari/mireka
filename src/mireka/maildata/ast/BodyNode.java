package mireka.maildata.ast;

import mireka.maildata.io.Subsource;
import mireka.maildata.type.MediaType;

public abstract class BodyNode {
    public MediaType mediaType;
    public Subsource subsource;
}
