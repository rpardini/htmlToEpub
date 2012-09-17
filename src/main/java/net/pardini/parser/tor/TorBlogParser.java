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

        Map<String, List<Chapter>> fullBook = new LinkedHashMap<String, List<Chapter>>();
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
        String htmlURL = "http://www.tor.com/features/series/wot-reread";
        Document document = parseDocumentFromURL(htmlURL);

        Map<String, Map<String, String>> fullMap = new LinkedHashMap<String, Map<String, String>>();

        Elements elements = document.select("div.group_with_image");
        for (Element element : elements) {
            Element titleElement = element.select("div.title em").get(0);
            String bookTitle = titleElement.text();

            Map<String, String> urlMap = new LinkedHashMap<String, String>();

            Elements links = element.select("a");
            for (Element link : links) {
                String href = link.attr("abs:href");
                String fullId = link.text();
                log.debug(String.format("Adding '%s' '%s' to url '%s'", bookTitle, fullId, href));

                urlMap.put(fullId, href);
            }

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
        public List<RereadBook> bookList = new ArrayList<RereadBook>();
    }

    public class RereadBook {
        public final String bookTitle;
        public List<Chapter> chapterList = new ArrayList<Chapter>();


        public RereadBook(final String bookTitle) {
            this.bookTitle = bookTitle;
        }
    }

    public class Chapter {
        public final String bookName;
        public final String title;
        public final String url;
        public String html;
        public Set<String> images = new HashSet<String>();
        public List<ChapterSection> sections;

        public Chapter(final String bookName, final String title, final String url) {
            log.debug(String.format("Constructing Chapter for book '%s' title '%s' and url '%s'", bookName, title, url));
            this.bookName = bookName;
            this.title = title;
            this.url = url;
        }

        public void parseHTML() throws Exception {
            Map<String, String> mappedFixes = new HashMap<String, String>();
            mappedFixes.put("<span style=\"font-size:14px;&ldquo;>", "<span style=\"font-size:14px;\">");
            mappedFixes.put("<span style=\"font-size: 14px;&ldquo;>", "<span style=\"font-size:14px;\">");
            mappedFixes.put("<span new=\"\" roman=\"\" style=\"font-family: Georgia;\" times=\"\"> </span>", "");


            Document document = parseDocumentFromURL(url, mappedFixes);
            Element mainElement = document.select("div.content div.text").get(0);

            Elements allImages = mainElement.select("img");
            for (Element oneImage : allImages) {
                String src = StringUtils.trimToNull(oneImage.attr("abs:src"));
                if (src != null) {
                    log.debug(String.format("Found image link: %s", src));
                    String md5 = CacheUtils.getIDForImage(src);
                    oneImage.attr("src", md5);
                    images.add(src);
                }
            }


            List<ChapterSection> chapterSectionList = new ArrayList<ChapterSection>();

            Elements allPossibleTitleElements = new Elements();
            allPossibleTitleElements.addAll(mainElement.select("p strong u"));
            allPossibleTitleElements.addAll(mainElement.select("p u strong"));

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

            this.sections = new ArrayList<ChapterSection>();
            this.sections.add(previousSection);

            for (Element directChild : directChildren) {
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
                    accumulatedElements.add(directChild);
                }

            }

            previousSection.elements = new Elements();
            previousSection.elements.add(createTitleElement(mainElement, previousSection));
            previousSection.elements.addAll(accumulatedElements);

            this.sections.addAll(chapterSectionList);

            this.html = mainElement.html();
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
