package mireka.transmission.queue.dataprop;

import java.net.InetAddress;
import java.net.UnknownHostException;

class InetAddressParser {
    private final String inputValue;

    public InetAddressParser(String s) {
        this.inputValue = s;
    }

    public InetAddress parse() {
        try {
            String s = inputValue.trim();
            if (s.isEmpty())
                throw new RuntimeException("Illegal InetAddress");
            if (s.charAt(0) == '/') {
                return InetAddress.getByName(s.substring(1));
            } else {
                int iSlash = s.indexOf('/');
                if (iSlash == -1)
                    throw new RuntimeException("Illegal InetAddress");
                String name = s.substring(0, iSlash);
                String address = s.substring(iSlash + 1);
                InetAddress inetAddress = InetAddress.getByName(address);
                return InetAddress.getByAddress(name, inetAddress.getAddress());
            }
        } catch (UnknownHostException e) {
            // impossible
            throw new RuntimeException();
        }
    }
}