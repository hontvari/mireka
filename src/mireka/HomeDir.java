package mireka;

import java.io.File;

import javax.enterprise.inject.Produces;
import javax.inject.Named;

/**
 * HomeDir is a CDI factory which injects the Mireka home directory location.
 * This directory contains the conf, log etc. subdirectories.
 */
public class HomeDir {
    private File dir;

    @Produces
    @Named("mirekaHome")
    public File get() {
        return dir;
    }

    /**
     * @category GETSET
     */
    public void setPath(String dir) {
        this.dir = new File(dir);
    }

}
