package mireka.smtp.address.parser;

import static org.junit.Assert.*;
import mireka.smtp.address.parser.ReversePathParser;
import mireka.smtp.address.parser.ast.NullReversePathAST;
import mireka.smtp.address.parser.ast.RealReversePathAST;
import mireka.smtp.address.parser.ast.ReversePathAST;

import org.junit.Test;

public class ReversePathTest {
    @Test
    public void testNullReversePath() throws Exception {
        ReversePathAST reversePathAST = new ReversePathParser("<>").parse();
        assertTrue(reversePathAST instanceof NullReversePathAST);
    }

    @Test
    public void testNonNullReversePath() throws Exception {
        ReversePathAST reversePathAST =
                new ReversePathParser("<john@example.com>").parse();
        assertTrue(reversePathAST instanceof RealReversePathAST);
        assertEquals(
                "john",
                ((RealReversePathAST) reversePathAST).pathAST.mailboxAST.localPartAST.spelling);

    }
}
