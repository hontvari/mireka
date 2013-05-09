package mireka.smtp.client;

import java.io.IOException;

import org.subethamail.smtp.client.SMTPException;
import org.subethamail.smtp.client.SmartClient;

/**
 * SmtpClient extends SmartClient so that it requires no additional information
 * to connect to a client.
 */
public class SmtpClient extends SmartClient {
    private MtaAddress mtaAddress;
    
    public void connect() throws SMTPException, IOException {
    	super.setHostPort(mtaAddress.toString());
        super.connect(mtaAddress.address.getHostAddress(), mtaAddress.port);
    }

    public MtaAddress getMtaAddress() {
        return mtaAddress;
    }

    public void setMtaAddress(MtaAddress mtaAddress) {
        this.mtaAddress = mtaAddress;
    }
    
}
