package mireka.maildata.field;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import mireka.maildata.Address;
import mireka.maildata.FieldGenerator;
import mireka.maildata.HeaderField;
import mireka.util.CharsetUtil;

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
    protected void writeGenerated(OutputStream out) throws IOException {
        String result = new FieldGenerator().writeAddressListField(this);
        out.write(CharsetUtil.toAsciiBytes(result));
    }

}
