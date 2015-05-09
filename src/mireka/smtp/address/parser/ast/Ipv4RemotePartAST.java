package mireka.smtp.address.parser.ast;

import mireka.smtp.address.parser.Ipv4Parser.Ipv4;

public class Ipv4RemotePartAST extends AddressLiteralRemotePartAST {
    public Ipv4 ipv4;

    public Ipv4RemotePartAST(int position, String spelling, Ipv4 ipv4) {
        super(position, spelling);
        this.ipv4 = ipv4;
    }
}