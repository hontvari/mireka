package mireka;

import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.extensions.cpsuite.ClasspathSuite.ClassnameFilters;
import org.junit.runner.RunWith;

@RunWith(ClasspathSuite.class)
@ClassnameFilters( { "!mireka.transmission.queue.DirectoryListSpeedTest",
        "!mireka.transmission.dsn.DnsMailCreatorLargeOriginalTest" })
public class QuickTests {
    // only annotations are relevant
}
