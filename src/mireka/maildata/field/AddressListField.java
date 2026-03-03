package mireka.maildata.field;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mireka.maildata.HeaderField;
import mireka.maildata.parser.FieldGenerator;
import mireka.maildata.parser.Kind;
import mireka.maildata.type.Address;

/**
 * AddressListField represents header fields which consists of an address-field nonterminal, which
 * means a list of mailboxes and groups, for example From and To fields.
 */
public class AddressListField extends HeaderField {

    public AddressListField(Kind kind) {
        super(kind);
    }

    public List<Address> addressList = new ArrayList<>();

    @Override
    protected String generate() throws IOException {
        return new FieldGenerator().writeAddressListField(this);
    }

}
