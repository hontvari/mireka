package mireka.filter;

public interface FilterType {
    Filter createInstance(MailTransaction mailTransaction);
}
