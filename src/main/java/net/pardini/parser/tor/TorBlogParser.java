package net.pardini.parser.tor;

import net.pardini.parser.BaseHtmlParser;
import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
                String linkTitle = link.text();

                String fullId = linkTitle;
                log.info(String.format("Adding '%s' '%s' to url '%s'", bookTitle, fullId, href));
                urlMap.put(fullId, href);
            }

            fullMap.put(bookTitle, urlMap);

        }
        return fullMap;
    }

    public Map<String, List<Chapter>> parseChapters() throws Exception {
        Map<String, Map<String, String>> fullMap = this.parseIndex();

        Map<String, List<Chapter>> fullBook = new LinkedHashMap<String, List<Chapter>>();


        for (String bookName : fullMap.keySet()) {
            List<Chapter> listChapters = new ArrayList<Chapter>();
            Map<String, String> bookChapters = fullMap.get(bookName);
            for (String title : bookChapters.keySet()) {
                String url = bookChapters.get(title);
                Chapter chapter = new Chapter(bookName, title, url);
                chapter.parseHTML();
                listChapters.add(chapter);
            }

            fullBook.put(bookName, listChapters);


        }
        return fullBook;
    }




    public class Chapter {

        public final String bookName;
        public final String title;
        public final String url;
        public String html;
        public Map<String, byte[]> images = new HashMap<String, byte[]>();

        public Chapter(final String bookName, final String title, final String url) {
            log.info(String.format("Constructing Chapter for book '%s' title '%s' and url '%s'", bookName, title, url));
            this.bookName = bookName;
            this.title = title;
            this.url = url;
        }

        public void parseHTML() throws Exception {
            Document document = parseDocumentFromURL(url);
            Element mainElement = document.select("div.content div.text").get(0);

            // Fix all a href links.
            Elements allLinks = mainElement.select("a");
            for (Element oneLink : allLinks) {
                String href = StringUtils.trimToNull(oneLink.attr("abs:href"));
                if (href!=null) {
                    log.info(String.format("Found link: %s", href));
                }
            }

            Elements allImages = mainElement.select("img");
            for (Element oneImage : allImages) {
                String src = StringUtils.trimToNull(oneImage.attr("abs:src"));
                if (src != null) {
                    log.info(String.format("Found image link: %s", src));
                }
            }


            this.html = mainElement.html();
        }


    }
}
