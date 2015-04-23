package mireka.maildata;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * AddressListField represents header fields which consists of an address-field
 * nonterminal, which means a list of mailboxes and groups.
 */
public class AddressListField extends HeaderField {

    public List<Address> addressList = new ArrayList<>();

    @Override
    protected void writeGenerated(OutputStream out) throws IOException {
        // TODO Auto-generated method stub

    }

}
