package mireka.maildata.parser;

import java.text.ParseException;

import mireka.maildata.HeaderField;
import mireka.maildata.HeaderFieldText;
import mireka.maildata.field.AddressListField;
import mireka.maildata.field.UnstructuredField;
import mireka.maildata.parser.FieldHeaderParser.FieldMap;

public class FieldParser {

    public static HeaderField parse(HeaderFieldText source) throws ParseException {
        FieldMap map = new FieldHeaderParser(source.unfoldedSpelling).parse();
        Kind kind = Kind.forHeaderFieldName(map.name);
        String body = source.unfoldedSpelling.substring(map.indexOfBody);
        HeaderField result;

        switch (kind) {
        case BCC:
        case CC:
        case FROM:
        case REPLY_TO:
        case RESENT_CC:
        case RESENT_FROM:
        case RESENT_TO:
        case SENDER:
        case TO:
            AddressListField addressListField = new AddressListField(kind);
            new StructuredFieldBodyParser(body).parseAddressListFieldInto(addressListField);
            result = addressListField;
            break;
        case MIME_VERSION:
            result = new StructuredFieldBodyParser(body).parseMimeVersion();
            break;
        case CONTENT_TYPE:
            result = new StructuredFieldBodyParser().parseContentType(body);
            break;
        default:
            UnstructuredField unstructuredField = new UnstructuredField(kind);
            result = unstructuredField;
        }
        result.name = map.name.original;
        result.source = source;
        result.bodyFull = new UnstructuredFieldBodyParser(body).parse();
        result.body = result.bodyFull.trim();
        return result;
    }
}
