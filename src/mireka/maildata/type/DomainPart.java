package mireka.maildata.type;

import javax.annotation.Nullable;

import mireka.maildata.parser.FieldGenerator;

public abstract class DomainPart {
    @Nullable
    public String spelling;

    public String generate() {
        FieldGenerator g = new FieldGenerator();
        g.writeDomain(this);
        return g.toString();
    }

    public static DomainPart parse(String s) {
        // TODO Auto-generated method stub
        return null;
    }
}
