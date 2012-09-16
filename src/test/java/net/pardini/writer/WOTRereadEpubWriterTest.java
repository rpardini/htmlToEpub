package net.pardini.writer;

import net.pardini.parser.tor.TorBlogParser;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Created with IntelliJ IDEA.
 * User: Pardini
 * Date: 15/09/12
 * Time: 20:41
 * To change this template use File | Settings | File Templates.
 */
public class WOTRereadEpubWriterTest {

    private TorBlogParser.RereadBooks fullBook;

    @BeforeMethod
    public void setUp() throws Exception {
        fullBook = new TorBlogParser().parseFullWOTRereadFromTorWebSite();
    }

    @Test
    public void testWriteCompleteEpub() throws Exception {
        WOTRereadEpubWriter wotRereadEpubWriter = new WOTRereadEpubWriter();
        wotRereadEpubWriter.writeCompleteEpub(fullBook);

    }

    @Test
    public void testWriteIndividualEbooksEpubs() throws Exception {

        for (TorBlogParser.RereadBook rereadBook : fullBook.bookList) {
            WOTRereadEpubWriter wotRereadEpubWriter = new WOTRereadEpubWriter();
            wotRereadEpubWriter.writeIndividualBookEpub(rereadBook);
        }
    }


}
