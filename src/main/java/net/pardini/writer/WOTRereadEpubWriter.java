package net.pardini.writer;

import net.pardini.parser.tor.TorBlogParser;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.TOCReference;

/**
 * Created with IntelliJ IDEA.
 * User: Pardini
 * Date: 15/09/12
 * Time: 20:31
 * To change this template use File | Settings | File Templates.
 */
public class WOTRereadEpubWriter extends BaseEPubWriter {
// --------------------------- CONSTRUCTORS ---------------------------

    public WOTRereadEpubWriter() {
        super();
        this.cssResource = "css/wotreread.css";
    }

// -------------------------- OTHER METHODS --------------------------

    public void writeCompleteEpub(final TorBlogParser.RereadBooks fullBook) throws Exception {
        startWOTBook("Wheel of Time Re-read (complete)");

        for (TorBlogParser.RereadBook rereadBook : fullBook.bookList) {
            TOCReference bookEntry = book.addSection(rereadBook.bookTitle, getHtmlForContentRes(rereadBook.bookTitle, rereadBook.bookTitle, null));

            for (TorBlogParser.Chapter chapter : rereadBook.chapterList) {
                log.debug(String.format("Writing chapter %s: %s", rereadBook.bookTitle, chapter.title));

                TorBlogParser.ChapterSection firstSection = chapter.sections.get(0);

                TOCReference firstSectionTOCReference = book.addSection(
                        bookEntry,
                        String.format("%s - %s", chapter.title, firstSection.sectionTitle),
                        getHtmlForContentRes(
                                firstSection.getHTML(),
                                String.format("%s - %s - %s", rereadBook.bookTitle, chapter.title, firstSection.sectionTitle),
                                chapter.url
                        )
                );


                for (TorBlogParser.ChapterSection section : chapter.sections) {
                    if (section.equals(firstSection)) continue;
                    book.addSection(
                            firstSectionTOCReference,
                            String.format("%s - %s", chapter.title, section.sectionTitle),
                            getHtmlForContentRes(
                                    section.getHTML(),
                                    String.format("%s - %s - %s", rereadBook.bookTitle, chapter.title, section.sectionTitle),
                                    chapter.url
                            )
                    );
                }

                addImagesForChapter(chapter);
            }
        }
        writeBookToFile();
    }

    private void startWOTBook(final String title) {
        Author author = new Author("Leigh", "Butler");
        Author contributor = new Author("Ricardo", "Pardini");

        startBook(title, author, contributor,
                "<p>This e-book is a compendium of blog posts written by Leigh Butler on <a href=\"http://www.tor.com\">tor.com</a>.</p>" +
                        "<p>All credit and copyright to Leigh Butler and Holtzbrinck Publishers, LLC (Macmillan).</p>" +
                        "<p>This is extraction v3!</p>");
    }

    private void addImagesForChapter(final TorBlogParser.Chapter chapter) {
        for (String imageURL : chapter.images) {
            images.add(imageURL);
        }
    }

    public void writeIndividualBookEpub(final TorBlogParser.RereadBook rereadBook) throws Exception {
        startWOTBook(String.format("WoT Reread - %s", rereadBook.bookTitle));

        for (TorBlogParser.Chapter chapter : rereadBook.chapterList) {
            log.debug(String.format("Writing chapter %s: %s", rereadBook.bookTitle, chapter.title));

            TorBlogParser.ChapterSection firstSection = chapter.sections.get(0);

            TOCReference firstSectionTOCReference = book.addSection(
                    String.format("%s - %s", chapter.title, firstSection.sectionTitle),
                    getHtmlForContentRes(
                            firstSection.getHTML(),
                            String.format("%s - %s - %s", rereadBook.bookTitle, chapter.title, firstSection.sectionTitle),
                            chapter.url
                    )
            );


            for (TorBlogParser.ChapterSection section : chapter.sections) {
                if (section.equals(firstSection)) continue;
                book.addSection(
                        firstSectionTOCReference,
                        String.format("%s - %s", chapter.title, section.sectionTitle),
                        getHtmlForContentRes(
                                section.getHTML(),
                                String.format("%s - %s - %s", rereadBook.bookTitle, chapter.title, section.sectionTitle),
                                chapter.url
                        )
                );
            }

            addImagesForChapter(chapter);
        }
        writeBookToFile();
    }
}
