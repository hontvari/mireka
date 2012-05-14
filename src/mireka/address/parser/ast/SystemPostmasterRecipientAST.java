package mireka.address.parser.ast;

public class SystemPostmasterRecipientAST extends RecipientAST {
    public String postmasterSpelling;

    public SystemPostmasterRecipientAST(int position, String postmasterSpelling) {
        super(position);
        this.postmasterSpelling = postmasterSpelling;
    }

}