package mireka.smtp.address.parser.ast;

import java.net.InetAddress;

public abstract class AddressLiteralRemotePartAST extends RemotePartAST {
    public byte[] addressBytes;
    public InetAddress address;

    public AddressLiteralRemotePartAST(int position, String spelling) {
        super(position, spelling);
    }

}