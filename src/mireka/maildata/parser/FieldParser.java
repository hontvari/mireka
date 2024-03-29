package mireka.maildata.parser;

import static mireka.util.CharsetUtil.*;

import java.text.ParseException;

import mireka.maildata.HeaderField;
import mireka.maildata.field.AddressListField;
import mireka.maildata.field.Cc;
import mireka.maildata.field.From;
import mireka.maildata.field.ReplyTo;
import mireka.maildata.field.ResentCc;
import mireka.maildata.field.ResentTo;
import mireka.maildata.field.To;
import mireka.maildata.parser.FieldHeaderParser.FieldMap;

public class FieldParser {

    public static HeaderField parse(String unfoldedField) throws ParseException {
        FieldMap map = new FieldHeaderParser(unfoldedField).parse();

        String body = unfoldedField.substring(map.indexOfBody);
        String lowerCaseName = toAsciiLowerCase(map.name);
        HeaderField result;

        switch (lowerCaseName) {
        case "from":
            AddressListField addressListField = new From();
            new StructuredFieldBodyParser(body)
                    .parseAddressListFieldInto(addressListField);
            result = addressListField;
            break;
        case "reply-to":
            addressListField = new ReplyTo();
            new StructuredFieldBodyParser(body)
                    .parseAddressListFieldInto(addressListField);
            result = addressListField;
            break;
        case "to":
            result = addressListField = new To();
            new StructuredFieldBodyParser(body)
                    .parseAddressListFieldInto(addressListField);
            break;
        case "cc":
            result = addressListField = new Cc();
            new StructuredFieldBodyParser(body)
                    .parseAddressListFieldInto(addressListField);
            break;
        case "resent-to":
            result = addressListField = new ResentTo();
            new StructuredFieldBodyParser(body)
                    .parseAddressListFieldInto(addressListField);
            break;
        case "resent-cc":
            result = addressListField = new ResentCc();
            new StructuredFieldBodyParser(body)
                    .parseAddressListFieldInto(addressListField);
            break;
        case "mime-version":
            result = new StructuredFieldBodyParser(body).parseMimeVersion();
            break;
        case "content-type":
            result = new StructuredFieldBodyParser().parseContentType(body);
            break;
        default:
            result = new UnstructuredFieldBodyParser(body).parse();
        }
        result.setName(map.name);
        return result;
    }
}
