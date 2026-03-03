package mireka.sieve.ast;

import static mireka.sieve.Kind.All;

import java.util.ArrayList;
import java.util.List;

import mireka.maildata.type.Mailbox;
import mireka.sieve.Kind;

/**
 * Represents the :all, :localpart, :domain tags, which specify which part of an email address must
 * be compared in a comparison.
 */
public class AddressPart {
    public boolean specified;
    /**
     * Possible other values: {@link Kind#Localpart}, {@link Kind#Domain}
     */
    public Kind value = All;

    /**
     * Returns the selected address part in multiple form. The local part may be in encoded and
     * in displayable form, e.g. '"John Doe"' including double quotes as appears in the fields
     * and as 'John Doe' as intended for display.
     */
    public List<String> extract(Mailbox mailbox) {
        List<String> result = new ArrayList<>();
        switch (value) {
        case All:
            result.add(mailbox.addrSpec.spelling);
            result.add(
                    mailbox.addrSpec.localPart.value + "@" + mailbox.addrSpec.domain.spelling);
            result.add(mailbox.addrSpec.generate());
            break;
        case Localpart:
            result.add(mailbox.addrSpec.localPart.spelling);
            result.add(mailbox.addrSpec.localPart.value);
            result.add(mailbox.addrSpec.localPart.generate());
            break;
        case Domain:
            result.add(mailbox.addrSpec.domain.spelling);
            result.add(mailbox.addrSpec.domain.generate());
            break;
        default:
            assert false;
        }
        return result;
    }
}