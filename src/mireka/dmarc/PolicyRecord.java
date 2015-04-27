package mireka.dmarc;

public class PolicyRecord {
    public Request request;

    public enum Request {
        none, quarantine, reject;
    }
}
