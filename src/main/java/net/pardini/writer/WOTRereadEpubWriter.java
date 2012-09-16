package net.pardini.writer;

import net.pardini.parser.BaseHtmlParser;
import net.pardini.parser.CacheUtils;
import net.pardini.parser.tor.TorBlogParser;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubWriter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Pardini
 * Date: 15/09/12
 * Time: 20:31
 * To change this template use File | Settings | File Templates.
 */
public class WOTRereadEpubWriter {
// ------------------------------ FIELDS ------------------------------

    private final Charset utf8 = Charset.forName("UTF8");
    private int chapterCounter = 0;

    private final Logger log = LoggerFactory.getLogger(getClass());

// -------------------------- OTHER METHODS --------------------------

    public void writeEpub(final Map<String, List<TorBlogParser.Chapter>> fullBook) throws Exception {
        new File("target\\html").mkdirs();

        Book book = new Book();

        // Set the title
        book.getMetadata().addTitle("Wheel of Time Re-read");

        // Add an Author
        book.getMetadata().addAuthor(new Author("Leigh", "Butler"));
        book.getMetadata().addContributor(new Author("Ricardo", "Pardini"));
        book.getMetadata().addDate(new nl.siegmann.epublib.domain.Date(new Date(), nl.siegmann.epublib.domain.Date.Event.CREATION));

        // Set cover image
        //book.getMetadata().setCoverImage(new Resource(getClass().getResourceAsStream("/book1/test_cover.png"), "cover.png"));


        Set<String> images = new HashSet<String>();

        for (String bookTitle : fullBook.keySet()) {
            List<TorBlogParser.Chapter> chapters = fullBook.get(bookTitle);
            TOCReference bookEntry = book.addSection(bookTitle, getHtmlForContentRes(bookTitle, bookTitle));

            for (TorBlogParser.Chapter chapter : chapters) {
                log.info(String.format("Writing chapter %s: %s", bookTitle, chapter.title));
                Resource htmlForContentRes = getHtmlForContentRes(chapter.html, String.format("%s - %s", bookTitle, chapter.title));
                FileUtils.writeByteArrayToFile(new File(String.format("target\\html\\%d - %s - %s.html", chapterCounter, bookTitle, chapter.title)), htmlForContentRes.getData());
                book.addSection(bookEntry, chapter.title, htmlForContentRes);

                for (String imageURL : chapter.images) {
                    images.add(imageURL);
                }
            }
        }

        BaseHtmlParser parser = new BaseHtmlParser();

        // Now get and add all images!
        for (String imageURL : images) {
            String imageID = CacheUtils.getIDForImage(imageURL);
            book.addResource(new Resource(parser.getURLCachedByteArray(imageURL), imageID));
        }


        // Create EpubWriter
        EpubWriter epubWriter = new EpubWriter();

        // Write the Book as Epub
        epubWriter.write(book, new FileOutputStream("target\\WOT Reread.epub"));
    }

    public Resource getHtmlForContentRes(String content, String title) {
        chapterCounter++;
        return new Resource(this.getHtmlDocForContent(content, title), String.format("chapter%d.html", chapterCounter));
    }

    public byte[] getHtmlDocForContent(String content, String title) {
        String doc = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE html\n" +
                "        PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n" +
                "        \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n" +
                "<head>\n" +
                "    <title>%s</title>\n" +
                "</head>\n" +
                "<body>%s</body>" +
                "</html>", title, content);
        return doc.getBytes(utf8);
    }

}
