package mireka;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.mail.internet.ParseException;

import mireka.address.Domain;
import mireka.address.GlobalPostmaster;
import mireka.address.MailAddressFactory;
import mireka.address.Recipient;

import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;

public class ExampleAddress {
    /**
     * host.example.com [192.0.2.0]
     */
    public static final InetAddress IP;
    /**
     * 
     */
    public static final InetAddress IP1;
    public static final InetAddress IP2;
    public static final InetAddress IP3;
    public static final String IP_STRING = "192.0.2.0";
    public static final InetAddress IP_ADDRESS_ONLY;
    public static final InetAddress IPV6;
    public static final String ADDRESS_LITERAL = "[192.0.2.0]";
    public static final String JANE = "jane@example.com";
    public static final String JOHN = "john@example.com";
    public static final String NANCY_NET = "nancy@example.net";
    public static final Recipient JANE_AS_RECIPIENT;
    public static final Recipient JOHN_AS_RECIPIENT;
    public static final Recipient NANCY_NET_AS_RECIPIENT;
    public static final String ADA_ADDRESS = "ada@[" + IP_STRING + "]";
    public static final Recipient ADA_ADDRESS_LITERAL_AS_RECIPIENT;
    public static final String ALBERT_ADDRESS = "albert@[" + IP_STRING + "]";
    public static final Recipient ALBERT_ADDRESS_LITERAL_AS_RECIPIENT;
    public static final GlobalPostmaster GLOBAL_POSTMASTER_AS_RECIPIENT =
            new GlobalPostmaster("Postmaster");
    public static final String EXAMPLE_COM_ABSOLUTE = "example.com.";
    public static final String EXAMPLE_COM = "example.com";
    public static final Domain EXAMPLE_COM_DOMAIN = new Domain("example.com");
    public static final Name EXAMPLE_COM_NAME;
    public static final String HOST1_EXAMPLE_COM = "host1.example.com";
    public static final String HOST2_EXAMPLE_COM = "host2.example.com";
    public static final String HOST3_EXAMPLE_COM = "host3.example.com";
    public static final String HOST4_EXAMPLE_COM = "host4.example.com";
    public static final String HOST6_EXAMPLE_COM = "host6.example.com";
    public static final Name HOST1_EXAMPLE_COM_NAME;
    public static final Name HOST2_EXAMPLE_COM_NAME;
    public static final Name HOST3_EXAMPLE_COM_NAME;
    public static final Name HOST4_EXAMPLE_COM_NAME;
    public static final Name HOST6_EXAMPLE_COM_NAME;

    static {
        try {
            IP =
                    InetAddress.getByAddress("host.example.com", new byte[] {
                            (byte) 192, 0, 2, 0 });
            IP1 =
                    InetAddress.getByAddress("host1.example.com", new byte[] {
                            (byte) 192, 0, 2, 1 });
            IP2 =
                    InetAddress.getByAddress("host2.example.com", new byte[] {
                            (byte) 192, 0, 2, 2 });
            IP3 =
                    InetAddress.getByAddress("host3.example.com", new byte[] {
                            (byte) 192, 0, 2, 3 });
            IP_ADDRESS_ONLY = InetAddress.getByName("192.0.2.0");
            IPV6 =
                    InetAddress.getByAddress(HOST6_EXAMPLE_COM, new byte[] {
                            0x20, 0x01, 0x0D, (byte) 0xB8, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0 });
            JANE_AS_RECIPIENT = new MailAddressFactory().createRecipient(JANE);
            JOHN_AS_RECIPIENT = new MailAddressFactory().createRecipient(JOHN);
            NANCY_NET_AS_RECIPIENT =
                    new MailAddressFactory().createRecipient(NANCY_NET);
            ADA_ADDRESS_LITERAL_AS_RECIPIENT =
                    new MailAddressFactory().createRecipient(ADA_ADDRESS);
            ALBERT_ADDRESS_LITERAL_AS_RECIPIENT =
                    new MailAddressFactory().createRecipient(ALBERT_ADDRESS);
            EXAMPLE_COM_NAME = new Name(EXAMPLE_COM_ABSOLUTE);
            HOST1_EXAMPLE_COM_NAME = new Name(HOST1_EXAMPLE_COM + ".");
            HOST2_EXAMPLE_COM_NAME = new Name(HOST2_EXAMPLE_COM + ".");
            HOST3_EXAMPLE_COM_NAME = new Name(HOST3_EXAMPLE_COM + ".");
            HOST4_EXAMPLE_COM_NAME = new Name(HOST4_EXAMPLE_COM + ".");
            HOST6_EXAMPLE_COM_NAME = new Name(HOST6_EXAMPLE_COM + ".");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (TextParseException e) {
            throw new RuntimeException(e);
        }
    }

}
