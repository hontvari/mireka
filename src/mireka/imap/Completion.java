package mireka.imap;

import javax.annotation.Nullable;

import mireka.imap.command.Command;

public class Completion {
    public static final Completion CONTINUE = new Completion();
    public static final Completion OK = new Completion();
    public static final Completion OK_LOGOUT = new Completion();

    /**
     * Either OK, NO, BAD. It can be null in case of CONTINUE.
     */
    @Nullable
    public String status;
    @Nullable
    public ResponseCode code;
    @Nullable
    public String humanReadableText;
    /**
     * true if the command processing will be continued asynchronously in
     * {@link Command#asyncExecute()}
     */
    public boolean continueAsync;
    /**
     * The connection must be terminated after the completion response is sent
     */
    public boolean logout;
    
    static {
        CONTINUE.continueAsync = true;
        OK.status = "OK";
        OK_LOGOUT.status = "OK";
        OK_LOGOUT.logout = true;
    }

    private Completion() {
        // do nothing
    }

    public static Completion ok(ResponseCode code, String humanReadableText) {
        Completion r = new Completion();
        r.status = "OK";
        r.code = code;
        r.humanReadableText = humanReadableText;
        return r;
    }

    public static Completion ok(ResponseCode code) {
        return ok(code, null);
    }


    public static Completion no(String humanReadableText) {
        Completion r = new Completion();
        r.status = "NO";
        r.humanReadableText = humanReadableText;
        return r;
    }
}
