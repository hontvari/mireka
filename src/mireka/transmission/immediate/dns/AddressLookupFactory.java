package mireka.transmission.immediate.dns;

import org.xbill.DNS.Name;

public class AddressLookupFactory {
    public AddressLookup create(Name name) {
        return new AddressLookup(name);
    }
}
