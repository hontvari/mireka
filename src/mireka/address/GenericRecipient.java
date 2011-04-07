package mireka.address;

/**
 * This class represents a generic recipient, which is neither the special
 * global nor the special domain specific postmaster address.
 */
public class GenericRecipient implements RemotePartContainingRecipient {
    private final Address address;

    public GenericRecipient(Address address) {
        this.address = address;
    }

    public boolean isPostmaster() {
        return false;
    }

    public boolean isGlobalPostmaster() {
        return false;
    }

    public boolean isDomainPostmaster() {
        return false;
    }

    @Override
    public LocalPart localPart() {
        return address.getLocalPart();
    }

    public String sourceRouteStripped() {
        return address.toString();
    }

    public Address getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return address.toString();
    }
}
