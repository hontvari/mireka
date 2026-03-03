package mireka.sieve.ast;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mireka.maildata.field.AddressListField;
import mireka.maildata.parser.Kind;
import mireka.maildata.type.Address;
import mireka.maildata.type.Group;
import mireka.maildata.type.Mailbox;
import mireka.sieve.Context;
import mireka.sieve.Interpreter.Token;

/**
 * Address test command
 */
public class AddressTc extends Testcommand {
    private final Logger logger = LoggerFactory.getLogger(AddressTc.class);
    public ComparatorType comparator = new ComparatorType();
    public MatchType matchType = new MatchType();
    public AddressPart addressPart = new AddressPart();
    public List<Kind> headers = new ArrayList<>();
    public List<String> keys = new ArrayList<>();

    public AddressTc(Token token) {
        super(token);
    }

    @Override
    public boolean test(Context context) {
        trace();
        try {
            List<String> values = new ArrayList<>();
            for (Kind header : headers) {
                List<AddressListField> fields = context.mail.maildata.headers().getAll(header,
                        AddressListField.class);

                for (AddressListField field : fields) {
                    for (Address address : field.addressList) {
                        if (address instanceof Mailbox) {
                            Mailbox mailbox = (Mailbox) address;
                            values.addAll(addressPart.extract(mailbox));
                        } else if (address instanceof Group) {
                            Group group = (Group) address;
                            for (Mailbox mailbox : group.mailboxList) {
                                values.addAll(addressPart.extract(mailbox));
                            }
                        } else {
                            assert false;
                        }
                    }
                }
            }
            logger.trace("Values to match: {}", values);
            return matchType.compare(comparator.get(), keys, values);
        } catch (ParseException e) {
            logger.error("Failed to parse mail: ", e);
            return false;
        }
    }
}
