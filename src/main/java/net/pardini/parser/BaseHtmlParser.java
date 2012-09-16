package net.pardini.parser;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Base class for our HTML parsers. Actually only provides one interesting method, that combines an HTTP client, caching, and jsoup parsing.
 */
public class BaseHtmlParser {
// ------------------------------ FIELDS ------------------------------

    protected final Logger log = LoggerFactory.getLogger(getClass());

// -------------------------- OTHER METHODS --------------------------

    public Document parseDocumentFromURL(final String htmlURL) throws Exception {
        String indexHtml = CacheUtils.getURLCached(htmlURL);
        return Jsoup.parse(indexHtml, htmlURL);
    }

    public Document parseDocumentFromURL(final String htmlURL, final Map<String, String> mappedFixes) throws Exception {
        String indexHtml = CacheUtils.getURLCached(htmlURL);

        for (String search : mappedFixes.keySet()) {
            String replace = mappedFixes.get(search);
            indexHtml = StringUtils.replace(indexHtml, search, replace);
        }

        return Jsoup.parse(indexHtml, htmlURL);
    }
}
