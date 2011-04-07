package mireka.address.parser.base;

public class Terminal extends AST {

    public String spelling;

    public Terminal(int position, String spelling) {
        super(position);
        this.spelling = spelling;
    }
}
