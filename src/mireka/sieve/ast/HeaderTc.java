package mireka.sieve.ast;

import static java.util.stream.Collectors.toList;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mireka.imap.CiString;
import mireka.maildata.HeaderField;
import mireka.sieve.Context;
import mireka.sieve.Interpreter.Token;

public class HeaderTc extends Testcommand {
    private final Logger logger = LoggerFactory.getLogger(AddressTc.class);
    public ComparatorType comparator = new ComparatorType();
    public MatchType matchType = new MatchType();
    public List<String> headers = new ArrayList<>();
    public List<String> keys = new ArrayList<>();

    public HeaderTc(Token token) {
        super(token);
    }

    @Override
    public boolean test(Context context) {
        trace();
        try {
            List<String> values = new ArrayList<>();
            for (String header : headers) {
                List<HeaderField> fields = context.mail.maildata.headers()
                        .getAll(new CiString(header));
                values.addAll(fields.stream().map(f -> f.bodyFull).collect(toList()));
            }
            logger.trace("Values to match: {}", values);
            return matchType.compare(comparator.get(), keys, values);
        } catch (ParseException e) {
            logger.error("Failed to parse mail: ", e);
            return false;
        }
    }
}
