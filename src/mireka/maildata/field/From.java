package mireka.maildata.field;


/**
 * Note: RFC 6854 changes the grammar rules for this field to enable usage of
 * groups, especially a single empty group.
 */
public class From extends AddressListField {

    public From() {
        super("From");
    }
}
