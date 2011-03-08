package mireka;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.concurrent.GuardedBy;

public class Version {
    @GuardedBy("Version.class")
    private static String cachedVersion;

    public static String getVersion() {
        // does not work (in Resin only?)
        // getClass().getPackage().getImplementationVersion()
        synchronized (Version.class) {
            if (cachedVersion == null) {
                try {
                    InputStream resourceAsStream =
                            Version.class
                                    .getResourceAsStream(
                                    "/version.properties");
                    Properties properties = new Properties();
                    properties.load(resourceAsStream);
                    resourceAsStream.close();
                    cachedVersion = properties.getProperty("version");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return cachedVersion;
        }
    }

}
