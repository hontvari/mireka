package mireka.filter.local.table;

import java.util.regex.Pattern;

import mireka.address.LocalPart;

/**
 * RegexLocalPart compares the local part of an address with the specified
 * regular expression.
 */
public class RegexLocalPart implements LocalPartSpecification {
    private String regex;
    private Pattern pattern;

    @Override
    public boolean isSatisfiedBy(LocalPart localPart) {
        return pattern.matcher(localPart.displayableName()).matches();
    }

    @Override
    public String toString() {
        return "localPart=regex:" + regex;
    }

    /**
     * @category GETSET
     */
    public String getRegex() {
        return regex;
    }

    /**
     * @category GETSET
     */
    public void setRegex(String regex) {
        this.regex = regex;
        this.pattern =
                Pattern.compile(regex, Pattern.CASE_INSENSITIVE
                        | Pattern.UNICODE_CASE);
    }
}
