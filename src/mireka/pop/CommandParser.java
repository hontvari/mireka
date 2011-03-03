package mireka.pop;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CommandParser extracts the command name from the command received from the
 * POP3 client and provides functions for parsing the arguments.
 */
public class CommandParser {
    private final String line;
    /**
     * It treats space as part of the single argument, and not as a separator.
     */
    private static Pattern singleExtendedArgumentCommandPattern = Pattern
            .compile("\\p{Graph}{3,4} (\\p{Print}+)");
    private static Pattern singleArgumentCommandPattern = Pattern
            .compile("\\p{Graph}{3,4} (\\p{Graph}+)");
    private static Pattern argumentPattern = Pattern.compile("\\p{Graph}+");

    public CommandParser(String line) {
        this.line = line;
    }

    public String parseSingleExtendedArgument() throws CommandSyntaxException {
        Matcher matcher = singleExtendedArgumentCommandPattern.matcher(line);
        if (!matcher.matches())
            throw new CommandSyntaxException(
                    "Syntax error: command with exactly one argument is expected");
        return matcher.group(1);
    }

    public String parseSingleArgument() throws CommandSyntaxException {
        Matcher matcher = singleArgumentCommandPattern.matcher(line);
        if (!matcher.matches())
            throw new CommandSyntaxException(
                    "Syntax error: command with exactly one argument is expected");
        return matcher.group(1);
    }

    public List<String> parseArguments() throws CommandSyntaxException {
        StringTokenizer tokenizer = new StringTokenizer(line, " ", true);
        // skip command
        if (!tokenizer.hasMoreTokens())
            throw new CommandSyntaxException(""); // empty command
        tokenizer.nextToken(); // command
        List<String> result = new ArrayList<String>();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (!token.equals(" "))
                throw new CommandSyntaxException(
                        "Syntax error: arguments must be separated by a "
                                + "single space character");
            if (!tokenizer.hasMoreTokens())
                throw new CommandSyntaxException(
                        "Syntax error: there are one or more spaces at the end of the command line");
            token = tokenizer.nextToken();
            if (!argumentPattern.matcher(token).matches())
                throw new CommandSyntaxException(
                        "Syntax error: an argument must consist of printable US-ASCII "
                                + "characters.");
            result.add(token);
        }
        return result;
    }

    public Integer parseSingleOptionalNumericArgument()
            throws CommandSyntaxException {
        List<String> args = parseArguments();
        if (args.isEmpty())
            return null;
        if (args.size() >= 2)
            throw new CommandSyntaxException(
                    "Syntax error: Either no argument or a "
                            + "single numeric argument is expected");
        try {
            return Integer.valueOf(args.get(0));
        } catch (NumberFormatException e) {
            throw new CommandSyntaxException(
                    "Syntax error: Either no argument or a "
                            + "single numeric argument is expected");
        }

    }

    public int parseSingleNumericArgument() throws CommandSyntaxException {
        List<String> args = parseArguments();
        if (args.size() != 1)
            throw new CommandSyntaxException(
                    "Syntax error: A single numeric argument is expected");
        try {
            return Integer.valueOf(args.get(0));
        } catch (NumberFormatException e) {
            throw new CommandSyntaxException(
                    "Syntax error: A single numeric argument is expected");
        }

    }

    public String extractCommand() throws CommandSyntaxException {
        StringBuilder buffer = new StringBuilder();
        int i = 0;
        while (true) {
            if (i >= line.length())
                break;
            char ch = line.charAt(i);
            if (ch == ' ')
                break;
            if (i >= 4)
                throw new CommandSyntaxException(
                        "Syntax error: command name is too long");
            buffer.append(ch);
            i++;
        }

        if (buffer.length() < 3)
            throw new CommandSyntaxException(
                    "Syntax error: command name is too short");
        return buffer.toString();
    }

}
