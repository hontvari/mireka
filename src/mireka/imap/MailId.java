package mireka.imap;

public class MailId {
    public final long uidvalidity;
    public final long uid;

    public MailId(long uidvalidity, long uid) {
        this.uidvalidity = uidvalidity;
        this.uid = uid;
    };
}
