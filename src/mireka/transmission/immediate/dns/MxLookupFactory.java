package mireka.transmission.immediate.dns;

import mireka.address.Domain;

public class MxLookupFactory {
    public MxLookup create(Domain domain) {
        return new MxLookup(domain);
    }
}
