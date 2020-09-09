package net.pardini.writer;

import net.pardini.parser.CacheUtils;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.tidy.Node;
import org.w3c.tidy.Tidy;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Pardini
 * Date: 16/09/12
 * Time: 12:01
 * To change this template use File | Settings | File Templates.
 */
public class BaseEPubWriter {
// ------------------------------ FIELDS ------------------------------

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final Set<String> images;
    protected String cssResource;
    protected String title;
    protected Book book;

    private final Charset utf8 = Charset.forName("UTF8");

// --------------------------- CONSTRUCTORS ---------------------------

    public BaseEPubWriter() {
        images = new LinkedHashSet<String>();
    }

// -------------------------- OTHER METHODS --------------------------

    public Resource getHtmlForContentRes(String content, String contentTitle, final String url) {
        String href = String.format("%s.html", contentTitle);
        href = StringUtils.replace(href, ":", "");
        log.debug(String.format("Writing HREF... '%s'", href));
        return new Resource(this.getHtmlDocForContent(content, contentTitle, url), href);
    }

    public byte[] getHtmlDocForContent(String content, String contentTitle, String url) {
        String doc = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE html\n" +
                "        PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n" +
                "        \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n" +
                "<head>\n" +
                "    <link rel=\"stylesheet\" href=\"styles.css\" type=\"text/css\" /> " +
                "    <title>%s</title>\n" +
                "</head>\n" +
                "<body>%s</body>" +
                "</html>", contentTitle, content);


        validateDoc(doc, contentTitle, url);


        return doc.getBytes(utf8);
    }

    private void validateDoc(final String doc, final String contentTitle, final String url) {
        Tidy tidy = new Tidy();
        tidy.setXHTML(true);


        StringWriter out = new StringWriter();
        tidy.setErrout(new PrintWriter(out));

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Node parse = tidy.parse(new StringReader(doc), byteArrayOutputStream);
        //tidy.setQuiet(true);

        String output = out.toString();
        String[] errLines = output.split("\\n");
        List<String> realErrors = new ArrayList<String>();
        for (String errLine : errLines) {
            errLine = StringUtils.trimToNull(errLine);
            if (errLine == null) continue;

            if (StringUtils.contains(errLine, "Doctype given is") ||
                    StringUtils.contains(errLine, "Document content looks like") ||
                    StringUtils.contains(errLine, "Warning: trimming empty") ||
                    StringUtils.contains(errLine, "no errors were found") ||
                    StringUtils.contains(errLine, "img lacks \"alt\" attribute") ||
                    StringUtils.contains(errLine, "longer descriptions should be given with") ||
                    StringUtils.contains(errLine, "which takes a URL linked to the description") ||
                    StringUtils.contains(errLine, "The alt attribute should be used to give a short description") ||
                    StringUtils.contains(errLine, "For further advice on how to make your pages accessible") ||
                    StringUtils.contains(errLine, "You may also want to try") ||
                    StringUtils.contains(errLine, "which is a free Web-based") ||
                    StringUtils.contains(errLine, "service for checking URLs for accessibility") ||
                    StringUtils.contains(errLine, "These measures are needed for") ||
                    StringUtils.contains(errLine, "no warnings or errors were found")


                    ) {
                continue;
            }
            realErrors.add(errLine);
        }

        if (realErrors.size() > 0) {
            for (String realError : realErrors) {
                log.error(String.format("Parse warnings '%s', url '%s' output '%s'", contentTitle, url, realError));
            }
        }
    }

    protected void startBook(final String title, final Author author, final Author contributor, final String extraDesc) {
        Book book = new Book();
        book.getMetadata().addTitle(title);
        book.getMetadata().addAuthor(author);
        book.getMetadata().addContributor(contributor);
        book.getMetadata().addDate(new nl.siegmann.epublib.domain.Date(new Date(), nl.siegmann.epublib.domain.Date.Event.CREATION));


        book.addSection("About this book", getHtmlForContentRes(
                String.format("<h1>%s</h1><h2>by %s %s</h2>%s", title, author.getFirstname(), author.getLastname(), extraDesc),
                "about_this_book", "http://"
        ));


        this.title = title;
        this.book = book;
    }

    protected void writeBookToFile() throws Exception {
        new File(String.format("target/html/%s", title)).mkdirs();

        // Now get and add all images!
        Resource largestImageResource = null;
        int largestImageSize = 0;



        for (String imageURL : images) {
            Resource resource = new Resource(CacheUtils.getURLCachedByteArray(imageURL), CacheUtils.getIDForImage(imageURL));
            book.addResource(resource);

            BufferedImage bimg = ImageIO.read(new ByteArrayInputStream(resource.getData()));
            int width          = bimg.getWidth();
            int height         = bimg.getHeight();
            int pixelsSquared = width * height;

            if (pixelsSquared > largestImageSize) {
                largestImageSize = pixelsSquared;
                largestImageResource = resource;
            }
        }

        // Set cover image
        if (largestImageResource != null) {
            book.setCoverImage(largestImageResource);
        }


        book.addResource(new Resource(getClass().getClassLoader().getResourceAsStream(cssResource), "styles.css"));


        Map<String, Resource> resourceMap = book.getResources().getResourceMap();
        for (String resourceID : resourceMap.keySet()) {
            log.debug(String.format("Resource ID: %s", resourceID));
            FileUtils.writeByteArrayToFile(new File(String.format("target/html/%s/%s", title, resourceID)), resourceMap.get(resourceID).getData());
        }


        // Create EpubWriter
        EpubWriter epubWriter = new EpubWriter();

        // Write the Book as Epub
        epubWriter.write(book, new FileOutputStream(String.format("target/%s.epub", title)));
    }
}
