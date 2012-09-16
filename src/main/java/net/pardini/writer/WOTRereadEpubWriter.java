package net.pardini.writer;

import net._01001111.text.LoremIpsum;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubWriter;

import java.io.FileOutputStream;
import java.nio.charset.Charset;

/**
 * Created with IntelliJ IDEA.
 * User: Pardini
 * Date: 15/09/12
 * Time: 20:31
 * To change this template use File | Settings | File Templates.
 */
public class WOTRereadEpubWriter {
    private final Charset utf8 = Charset.forName("UTF8");
    // -------------------------- OTHER METHODS --------------------------

    public void writeEpub() throws Exception {
        Book book = new Book();

        // Set the title
        book.getMetadata().addTitle("Epublib test book 2");

        // Add an Author
        book.getMetadata().addAuthor(new Author("Joe", "Tester"));

        // Set cover image
        //book.getMetadata().setCoverImage(new Resource(getClass().getResourceAsStream("/book1/test_cover.png"), "cover.png"));

        // Add Chapter 1
        Resource resource = new Resource(getHtmlDocForContent("Conteúdo maluco <b> bold</b>"), "chapter1.html");
        book.addSection("Introduction", resource);

        // Add css file
        //book.getResources().add(new Resource(getClass().getResourceAsStream("/book1/book1.css"), "book1.css"));

        // Add Chapter 2
        TOCReference chapter2 = book.addSection("Second Chapter", new Resource(getHtmlDocForContent("Capitulo dois"), "chapter2.html"));

        // Add image used by Chapter 2
        //book.getResources().add(new Resource(getClass().getResourceAsStream("/book1/flowers_320x240.jpg"), "flowers.jpg"));

        // Add Chapter2, Section 1
        book.addSection(chapter2, "Chapter 2, section 1", new Resource(getHtmlDocForContent("Capítulo dois seção um"), "chapter2_1.html"));

        // Add Chapter 3
        book.addSection("Conclusion", new Resource(getHtmlDocForContent("O fim do fim"), "chapter3.html"));

        // Create EpubWriter
        EpubWriter epubWriter = new EpubWriter();

        // Write the Book as Epub
        epubWriter.write(book, new FileOutputStream("test1_book1.epub"));
    }

    public byte[] getHtmlDocForContent(String content) {
        LoremIpsum jlorem = new LoremIpsum();
        String paragraphs = jlorem.paragraphs(33, true).replaceAll("\\n", "<br/>");

        String doc = String.format("<?xml version='1.0' encoding='utf-8'?>\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "  <head>\n" +
                "    <title>The Mistborn Trilogy</title>\n" +
                "    <meta content=\"http://www.w3.org/1999/xhtml; charset=utf-8\" http-equiv=\"Content-Type\"/>" +
                "</head>" +
                "<body>%s<br/>" +
                "<p>Outro parágrafo</p>" +
                "%s" +
                "</body>" +
                "</html>", content, paragraphs);
        return doc.getBytes(utf8);
    }


}
