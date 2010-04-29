package mireka.util;

public class Multiline {
    /**
     * Formats an SMTP multiline reply.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc5321#section-4.2.1">RFC 5321
     *      Simple Mail Transfer Protocol - 4.2.1. Reply Code Severities and
     *      Theory</a>
     */
    public static String prependStatusCodeToMessage(int code, String message) {
        MultilineParser parser = new MultilineParser(message);
        StringBuilder buffer = new StringBuilder();
        while (parser.hasNext()) {
            String line = parser.next();
            if (parser.atLastLine()) {
                buffer.append(code).append(' ').append(line);
            } else {
                buffer.append(code).append('-').append(line).append("\r\n");
            }
        }
        return buffer.toString();
    }
}
