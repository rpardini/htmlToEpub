package net.pardini.parser.tor;

import net.pardini.parser.BaseHtmlParser;
import net.pardini.parser.CacheUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pardini
 * Date: 15/09/12
 * Time: 19:27
 * To change this template use File | Settings | File Templates.
 */
public class TorBlogParser extends BaseHtmlParser {
// ------------------------------ FIELDS ------------------------------

    private final Logger log = LoggerFactory.getLogger(getClass());

// -------------------------- OTHER METHODS --------------------------

    public RereadBooks parseFullWOTRereadFromTorWebSite() throws Exception {
        Map<String, Map<String, String>> fullMap = this.parseIndex();
        RereadBooks rereadBooks = new RereadBooks();

        for (String bookName : fullMap.keySet()) {
            RereadBook rereadBook = new RereadBook(bookName);

            Map<String, String> bookChapters = fullMap.get(bookName);
            for (String title : bookChapters.keySet()) {
                String url = bookChapters.get(title);
                Chapter chapter = new Chapter(bookName, title, url);
                chapter.parseHTML();

                rereadBook.chapterList.add(chapter);
            }

            rereadBooks.bookList.add(rereadBook);
        }
        return rereadBooks;
    }

    public Map<String, Map<String, String>> parseIndex() throws Exception {
        String htmlURL = "https://www.tor.com/series/wot-reread/";
        Document document = parseDocumentFromURL(htmlURL);

        Map<String, Map<String, String>> fullMap = new LinkedHashMap<>();

        Elements elements = document.select("div.series-book");
        for (Element element : elements) {
            Element titleElement = element.select("div.series-title-book em").get(0);
            String bookTitle = titleElement.text();

            Map<String, String> urlMap = new LinkedHashMap<>();

            Elements links = element.select("a");
            for (Element link : links) {
                String href = link.attr("abs:href");
                String fullId = link.text();

                if (href.contains("redux")) {
                    log.debug("Ignoring REDUX link: {}", href);
                } else {
                    log.info(String.format("Adding '%s' '%s' to url '%s'", bookTitle, fullId, href));
                    urlMap.put(fullId, href);
                }
            }

            if (urlMap.size() > 0)
                fullMap.put(bookTitle, urlMap);
        }
        return fullMap;
    }

// -------------------------- INNER CLASSES --------------------------

    public class ChapterSection {
        public String sectionTitle;
        public Element separatorElement;
        public Elements elements;
        public Elements icons;

        public String getHTML() {
            return elements.outerHtml();
        }
    }

    public class RereadBooks {
        public List<RereadBook> bookList = new ArrayList<>();
    }

    public class RereadBook {
        public final String bookTitle;
        public List<Chapter> chapterList = new ArrayList<>();


        public RereadBook(final String bookTitle) {
            this.bookTitle = bookTitle;
        }
    }

    public class Chapter {
        public final String bookName;
        public final String title;
        public final String url;
        public Set<String> images = new HashSet<>();
        public List<ChapterSection> sections;

        public Chapter(final String bookName, final String title, final String url) {
            log.debug(String.format("Constructing Chapter for book '%s' title '%s' and url '%s'", bookName, title, url));
            this.bookName = bookName;
            this.title = title;
            this.url = url;
        }

        public void parseHTML() throws Exception {
            Map<String, String> mappedFixes = new HashMap<>();
            mappedFixes.put("<span style=\"font-size:14px;&ldquo;>", "<span style=\"font-size:14px;\">");
            mappedFixes.put("<span style=\"font-size: 14px;&ldquo;>", "<span style=\"font-size:14px;\">");
            mappedFixes.put("<span new=\"\" roman=\"\" style=\"font-family: Georgia;\" times=\"\"> </span>", "");


            Document document = parseDocumentFromURL(url, mappedFixes);
            Element mainElement = document.select("div#content article div.entry-content").get(0);

            Elements allImages = mainElement.select("img");
            for (Element oneImage : allImages) {
                String src = StringUtils.trimToNull(oneImage.attr("abs:src"));
                if (src != null) {
                    log.debug(String.format("Found image link (via normal src): %s", src));
                    String md5 = CacheUtils.getIDForImage(src);
                    oneImage.attr("src", md5);
                    images.add(src);
                }

                String srcSet = StringUtils.trimToNull(oneImage.attr("abs:srcset"));
                if (srcSet != null) {
                    log.debug(String.format("Found image link (via srcset): %s", src));
                    String md5 = CacheUtils.getIDForImage(srcSet);
                    oneImage.attr("srcset", "");
                    oneImage.attr("src", md5);
                    images.add(srcSet);
                }
            }


            List<ChapterSection> chapterSectionList = new ArrayList<>();

            Elements allPossibleTitleElements = new Elements();
            allPossibleTitleElements.addAll(mainElement.select("p strong u, p u strong, p span strong, p strong span"));

            for (Element titleElement : allPossibleTitleElements) {
                String sectionTitleText = StringUtils.trimToNull(titleElement.text());
                if (sectionTitleText == null) {
                    continue;
                }
                log.debug(String.format("Found title: %s - %s - %s", this.title, sectionTitleText, this.url));

                Element parentParagraph = findParentElementBefore(titleElement, mainElement);
                if (parentParagraph != null) {
                    log.debug("Found parent..." + parentParagraph.html());

                    ChapterSection section = new ChapterSection();
                    section.sectionTitle = sectionTitleText;
                    section.separatorElement = parentParagraph;

                    section.icons = parentParagraph.select("img");

                    // @TODO: sometimes there's more content to the 'separator' than just the title and the links, which is being lost right now.

                    chapterSectionList.add(section);

                }
            }

            Elements directChildren = mainElement.children();
            Elements accumulatedElements = new Elements();

            ChapterSection previousSection = new ChapterSection();
            previousSection.sectionTitle = "Intro";

            this.sections = new ArrayList<>();
            this.sections.add(previousSection);

            boolean foundEnd = false;

            for (Element directChild : directChildren) {
                if (directChild.hasClass("post-end-spacer")) {
                    log.info("Found end marking!");
                    foundEnd = true;
                }


                boolean isSeparator = false;

                for (ChapterSection section : chapterSectionList) {

                    if (section.separatorElement.equals(directChild)) {
                        isSeparator = true;

                        log.debug("Found splitter element...");

                        previousSection.elements = new Elements();
                        previousSection.elements.add(createTitleElement(mainElement, previousSection));
                        previousSection.elements.addAll(accumulatedElements);

                        accumulatedElements.clear();
                        previousSection = section;
                    }
                }

                if (!isSeparator) {
                    if (!foundEnd) {
                        accumulatedElements.add(directChild);
                    }
                }

            }

            previousSection.elements = new Elements();
            previousSection.elements.add(createTitleElement(mainElement, previousSection));
            previousSection.elements.addAll(accumulatedElements);

            this.sections.addAll(chapterSectionList);
        }

        private Element createTitleElement(final Element mainElement, final ChapterSection previousSection) {
            Element divOverall = new Element(Tag.valueOf("div"), mainElement.baseUri());

            if (previousSection.icons != null) {
                for (Element icon : previousSection.icons) {
                    icon.attr("class", "blog-pic-right-align");
                    divOverall.appendChild(icon);
                }
            }

            Element h1Title = new Element(Tag.valueOf("h1"), mainElement.baseUri());
            h1Title.text(String.format("%s - %s", this.title, previousSection.sectionTitle));

            divOverall.appendChild(h1Title);

            return divOverall;
        }

        private Element findParentElementBefore(final Element titleElement, final Element mainElement) {
            Element parent = titleElement.parent();
            Element last = parent;
            while (!parent.equals(mainElement)) {
                last = parent;
                parent = parent.parent();
            }
            return last;
        }

        private Element findParentElementOfTag(final Element titleElement, final String tagToFind) {
            Element parent = titleElement.parent();
            while (parent != null) {
                if (parent.tagName().equalsIgnoreCase(tagToFind)) {
                    return parent;
                }
                parent = parent.parent();
            }
            return null;
        }
    }
}
