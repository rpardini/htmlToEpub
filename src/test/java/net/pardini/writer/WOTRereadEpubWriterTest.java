package net.pardini.writer;

import org.testng.annotations.Test;

/**
 * Created with IntelliJ IDEA.
 * User: Pardini
 * Date: 15/09/12
 * Time: 20:41
 * To change this template use File | Settings | File Templates.
 */
public class WOTRereadEpubWriterTest {
    @Test
    public void testWriteEpub() throws Exception {
        WOTRereadEpubWriter wotRereadEpubWriter = new WOTRereadEpubWriter();
        wotRereadEpubWriter.writeEpub();

    }
}
