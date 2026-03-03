package mireka.maildata.ast;

import java.util.ArrayList;
import java.util.List;

import mireka.maildata.HeaderFieldText;
import mireka.maildata.io.Subsource;
import mireka.maildata.type.MediaType;

public class Fields {
    public Subsource subsource;
    public List<HeaderFieldText> texts = new ArrayList<>();

    public boolean isMime;
    public MediaType mediaType;

}
