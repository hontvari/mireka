package mireka.transmission.immediate;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mireka.smtp.EnhancedStatus;
import mireka.smtp.MailSystemStatus;
import mireka.util.MultilineParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.client.SMTPClient.Response;

public class ResponseParser {
    private static final Pattern pattern =
            Pattern.compile("\\A([245]\\." + "(0|([1-9]\\d{0,2}))\\."
                    + "(0|([1-9]\\d{0,2})))\\ ");
    private final Logger logger = LoggerFactory.getLogger(ResponseParser.class);

    public MailSystemStatus createResponseLookingForEnhancedStatusCode(
            Response originalResponse) {
        String enhancedStatusCode;
        try {
            enhancedStatusCode =
                    parseEnhancedStatusCode(originalResponse.getMessage());
        } catch (ParseException e) {
            return new Rfc821Status(originalResponse);
        }
        String message;
        try {
            message =
                    removeStatusCodesFromMessage(enhancedStatusCode,
                            originalResponse.getMessage());
        } catch (ParseException e) {
            logger.debug("Response seemingly contains an enhanced  "
                    + "status code, but it does not follow the syntax "
                    + "exactly.  "
                    + "Dropping the enhanced status information. "
                    + e.toString());
            return new Rfc821Status(originalResponse);
        }
        return new EnhancedStatus(originalResponse.getCode(),
                enhancedStatusCode, message);
    }

    private String parseEnhancedStatusCode(String message)
            throws ParseException {
        Matcher matcher = pattern.matcher(message);
        if (!matcher.lookingAt())
            throw new ParseException("Not a valid enhanced status code", 0);
        return matcher.group(1);
    }

    private String removeStatusCodesFromMessage(String statusCode,
            String message) throws ParseException {
        MultilineParser parser = new MultilineParser(message);
        StringBuilder buffer = new StringBuilder(message.length());
        while (parser.hasNext()) {
            String line = parser.next();
            if (!line.startsWith(statusCode))
                throw new ParseException("Line doesn't start with the "
                        + "expected status code: " + line, 0);
            line = line.substring(statusCode.length()).trim();
            if (!parser.atFirstLine())
                buffer.append("\r\n");
            buffer.append(line);
        }
        return buffer.toString();
    }
}
