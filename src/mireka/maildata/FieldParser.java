package mireka.maildata;

import static mireka.util.CharsetUtil.*;

import java.text.ParseException;

import mireka.maildata.FieldHeaderParser.FieldMap;

public class FieldParser {

    public static HeaderField parse(String unfoldedField) throws ParseException {
        FieldMap map = new FieldHeaderParser(unfoldedField).parse();

        String body = unfoldedField.substring(map.indexOfBody);
        String lowerCaseName = toAsciiLowerCase(map.name);
        HeaderField result;

        switch (lowerCaseName) {
        case "from":
            result = new StructuredFieldBodyParser(body).parseFromField();
            break;
        case "reply-to":
        case "to":
        case "cc":
        case "resent-to":
        case "resent-cc":
            result =
                    new StructuredFieldBodyParser(body).parseAddressListField();
            break;
        default:
            result = new UnstructuredFieldBodyParser(body).parse();
        }
        result.setName(map.name);
        return result;
    }
}
