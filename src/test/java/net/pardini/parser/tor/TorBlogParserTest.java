package net.pardini.parser.tor;


import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: pardini
 * Date: 15/09/12
 * Time: 19:32
 * To change this template use File | Settings | File Templates.
 */
public class TorBlogParserTest {
// -------------------------- OTHER METHODS --------------------------

    @Test
    public void testParseIndex() throws Exception {
        TorBlogParser torBlogParser = new TorBlogParser();
        Map<String, Map<String, String>> stringStringMap = torBlogParser.parseIndex();
        Assert.assertNotNull(stringStringMap);
        Assert.assertTrue(stringStringMap.keySet().size() > 0);
    }


    @Test
    public void testListChapters() throws Exception {
        TorBlogParser torBlogParser = new TorBlogParser();
        TorBlogParser.RereadBooks chapterList = torBlogParser.parseFullWOTRereadFromTorWebSite();
        Assert.assertNotNull(chapterList);

    }
}
