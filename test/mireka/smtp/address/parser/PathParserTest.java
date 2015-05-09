package mireka.smtp.address.parser;

import static org.junit.Assert.*;
import mireka.smtp.address.parser.PathParser;
import mireka.smtp.address.parser.ast.PathAST;

import org.junit.Test;

public class PathParserTest {
    @Test
    public void testSimplePath() throws Exception {
        PathAST pathAST = new PathParser("<john@example.com>").parse();
        assertNull(pathAST.sourceRouteAST);
        assertEquals("john", pathAST.mailboxAST.localPartAST.spelling);
        assertEquals("example.com", pathAST.mailboxAST.remotePartAST.spelling);
    }

    @Test
    public void testSourceRoute1() throws Exception {
        PathAST pathAST =
                new PathParser("<@example.org:john@example.com>").parse();
        assertEquals(1, pathAST.sourceRouteAST.domainASTs.size());
        assertEquals("example.org",
                pathAST.sourceRouteAST.domainASTs.get(0).spelling);
        assertEquals("john", pathAST.mailboxAST.localPartAST.spelling);
    }

    @Test
    public void testSourceRoute2() throws Exception {
        PathAST pathAST =
                new PathParser("<@example.org@example.net:john@example.com>")
                        .parse();
        assertEquals(2, pathAST.sourceRouteAST.domainASTs.size());
        assertEquals("example.org",
                pathAST.sourceRouteAST.domainASTs.get(0).spelling);
        assertEquals("john", pathAST.mailboxAST.localPartAST.spelling);
    }
}
