package mireka.filter.local.table;

import mireka.address.RemotePart;

/**
 * AnyRemotePart matches any remote part.
 */
public class AnyRemotePart implements RemotePartSpecification {

    @Override
    public boolean isSatisfiedBy(RemotePart remotePart) {
        return true;
    }

}
