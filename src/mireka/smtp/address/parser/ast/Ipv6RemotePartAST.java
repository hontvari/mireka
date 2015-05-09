package mireka.smtp.address.parser.ast;

import mireka.smtp.address.parser.Ipv6Parser;
import mireka.smtp.address.parser.Ipv6Parser.Ipv6;

public class Ipv6RemotePartAST extends AddressLiteralRemotePartAST {
    public Ipv6Parser.Ipv6 ipv6;

    public Ipv6RemotePartAST(int position, String spelling, Ipv6 ipv6) {
        super(position, spelling);
        this.ipv6 = ipv6;
    }
}