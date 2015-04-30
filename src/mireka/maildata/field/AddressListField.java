package mireka.maildata.field;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mireka.maildata.Address;
import mireka.maildata.HeaderField;
import mireka.maildata.parser.FieldGenerator;

/**
 * AddressListField represents header fields which consists of an address-field
 * nonterminal, which means a list of mailboxes and groups.
 */
public abstract class AddressListField extends HeaderField {

    public AddressListField() {
        super();
    }

    public AddressListField(String name) {
        super(name);
    }

    public List<Address> addressList = new ArrayList<>();

    @Override
    protected String generate() throws IOException {
        return new FieldGenerator().writeAddressListField(this);
    }

}
