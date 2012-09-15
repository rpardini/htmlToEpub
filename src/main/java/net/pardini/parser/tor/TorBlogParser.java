package net.pardini.parser.tor;

import net.pardini.parser.BaseHtmlParser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

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

    public Map<String, String> parseIndex() throws Exception {
        String htmlURL = "http://www.tor.com/features/series/wot-reread";
        Document document = parseDocumentFromURL(htmlURL);

        Map<String, String> urlMap = new HashMap<String, String>();

        Elements elements = document.select("div.group_with_image");
        for (Element element : elements) {
            Element titleElement = element.select("div.title em").get(0);
            String bookTitle = titleElement.text();

            Elements links = element.select("a");
            for (Element link : links) {
                String href = link.attr("abs:href");
                String linkTitle = link.text();
                
                String fullId = String.format("%s - %s", bookTitle, linkTitle);
                log.info(String.format("Adding '%s' to url '%s'", fullId, href));
                urlMap.put(fullId, linkTitle);
            }
        }

        return urlMap;
    }
}
