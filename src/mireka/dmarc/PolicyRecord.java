package mireka.dmarc;

/**
 * Parsed DMARC policy record. It contains only a small fraction of the semantic
 * information, which is enough to determine if the domain owner requests the
 * rejection of non-conforming mails.
 */
public class PolicyRecord {
    public Request request;

    public enum Request {
        none, quarantine, reject;
    }
}
