package mireka.imap;

import java.io.IOException;

import mireka.imap.parser.Generator;

public interface ResponseCode {
    public static final ResponseCode ALREADYEXISTS = of("ALREADYEXISTS");
    public static final ResponseCode INUSE = of("INUSE");
    public static final ResponseCode CORRUPTION = of("CORRUPTION");
    public static final ResponseCode NONEXISTENT = of("NONEXISTENT");
    public static final ResponseCode NOPERM = of("NOPERM");
    public static final ResponseCode UNAVAILABLE = of("UNAVAILABLE");
    public static final ResponseCode TRYCREATE = of("TRYCREATE");

    /**
     * Formats and sends this response code to the client.
     * 
     * An implementation should call the corresponding {@link Generator} write method.
     */
    void generate(Generator out) throws IOException;

    static ResponseCode of(String v) {
        return new ResponseCode() {

            @Override
            public void generate(Generator out) throws IOException {
                out.write(v);
            }
        };
    }

}
