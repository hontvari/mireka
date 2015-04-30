package mireka.maildata.parser;

import static org.junit.Assert.*;
import mireka.maildata.parser.Folder;

import org.junit.Test;

public class FolderTest {
    @Test
    public void testNoFolding() {
        String result = new Folder().t("From:").fsp(" ").begin().t("Jon")
                .fsp(" ").t("Postel").end().fsp(" ").t("<jon@example.org>")
                .toString();
        assertEquals(result, "From: Jon Postel <jon@example.org>\r\n");
    }

    @Test
    public void testHighLevelFolding() {
        String result = new Folder().setLimit(14, 1000).t("From:").fsp(" ")
                .begin().t("Jon").fsp(" ").t("Postel").end().fsp(" ")
                .t("<jon@example.org>").toString();
        assertEquals(result, "From:\r\n Jon Postel\r\n <jon@example.org>\r\n");
    }

    @Test
    public void testLowLevelFolding() {
        String result = new Folder().setLimit(5, 1000).t("From:").fsp(" ")
                .begin().t("Jon").fsp(" ").t("Postel").end().fsp(" ")
                .t("<jon@example.org>").toString();
        assertEquals(result,
                "From:\r\n Jon\r\n Postel\r\n <jon@example.org>\r\n");
    }

}
