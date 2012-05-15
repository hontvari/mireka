package mireka;

/**
 * Version retrieves the current Mireka version number.
 */
public class Version {

    public static String getVersion() {
        return Version.class.getPackage().getImplementationVersion();
    }
}
