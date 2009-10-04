package mireka.filter.builtin.proxy;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BackendClient {
    private String helo;
    private String bind;

    /**
     * @category GETSET
     */
    public String getHelo() {
        return helo;
    }

    /**
     * @category GETSET
     */
    public void setHelo(String helo) {
        this.helo = helo;
    }

    /**
     * @category GETSET
     */
    public String getBind() {
        return bind;
    }

    /**
     * @category GETSET
     */
    public void setBind(String bind) {
        this.bind = bind;
    }
    
    
}
