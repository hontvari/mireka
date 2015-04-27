package mireka.dmarc;

import java.text.ParseException;

import mireka.dmarc.PolicyRecord.Request;
import mireka.dmarc.tagvlist.TagValueList;
import mireka.dmarc.tagvlist.TagValueListParser;

public class PolicyRecordParser {

    public PolicyRecord parse(String recordString) throws ParseException {
        PolicyRecord result = new PolicyRecord();
        TagValueList tagValueList =
                new TagValueListParser().parse(recordString);
        if (tagValueList.list.size() < 1
                || !tagValueList.list.get(0).name.equals("v"))
            throw new ParseException("dmarc-version is expected, "
                    + recordString, 0);
        String version = tagValueList.get("v");
        if (!version.equals("DMARC1"))
            throw new ParseException(
                    "Not a DMARC record or unknown DMARC version, "
                            + recordString, 0);
        String policy = tagValueList.get("p");
        switch (policy) {
        case "none":
            result.request = Request.none;
            break;
        case "quarantine":
            result.request = Request.quarantine;
            break;
        case "reject":
            result.request = Request.reject;
            break;
        default:
            throw new ParseException("Unknown p value in " + recordString, 0);
        }

        return result;
    }
}
