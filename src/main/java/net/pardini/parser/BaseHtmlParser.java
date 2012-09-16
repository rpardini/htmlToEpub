package net.pardini.parser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class BaseHtmlParser {
// -------------------------- OTHER METHODS --------------------------

    private final Logger log = LoggerFactory.getLogger(getClass());


    public String getCachedStringById(String id) throws Exception {
        File cacheFile = getCacheFileById(CacheUtils.getHashForString(id));
        if (!cacheFile.exists()) {
            log.info(String.format("Cache file %s does not exist, cache nil.", cacheFile.toString()));
            return null;
        }
        return IOUtils.toString(cacheFile.toURI().toURL().openStream());
    }

    public byte[] getCachedByteArrayById(String id) throws Exception {
        File cacheFile = getCacheFileById(CacheUtils.getHashForString(id));
        if (!cacheFile.exists()) {
            log.info(String.format("Cache file %s does not exist, cache nil.", cacheFile.toString()));
            return null;
        }
        return IOUtils.toByteArray(cacheFile.toURI().toURL().openStream());
    }


    private File getCacheFileById(final String id) {
        File tempDir = new File("C:\\temp\\caches");
        if (!tempDir.exists()) {
            if (!tempDir.mkdirs()) throw new RuntimeException("Unable to create temp dir.");
        }
        String fullFileName = String.format("%s%s%s.html", tempDir.getAbsolutePath(), File.separator, id);
        //log.info(String.format("Cache filename for '%s' is '%s'", id, fullFileName));
        return new File(fullFileName);
    }

    public String getURLCached(String urlString) throws Exception {
        assert urlString != null;

        String content;
        content = getCachedStringById(urlString);
        if (content != null) {
            //log.info(String.format("Hit cache for '%s'", urlString));
            return content;
        }

        log.warn(String.format("Cache miss for '%s'", urlString));

        URL url = stringToURL(urlString);
        content = readURLContent(url);

        setCachedStringById(urlString, content);

        return content;
    }

    public byte[] getURLCachedByteArray(String urlString) throws Exception {
        assert urlString != null;

        byte[] content;
        content = getCachedByteArrayById(urlString);
        if (content != null) {
            //log.info(String.format("Hit cache for '%s'", urlString));
            return content;
        }

        log.warn(String.format("Cache miss for '%s'", urlString));

        URL url = stringToURL(StringUtils.replace(urlString, " ", "%20"));
        content = readURLToByteArray(url);
        setCachedByteArrayById(urlString, content);
        return content;
    }


    private URL stringToURL(final String urlString) {
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw new RuntimeException(String.format("Error parsing URL %s: %s", url, e.getMessage()), e);
        }
        return url;
    }

    private String readURLContent(final URL url) throws IOException, URISyntaxException {
        return readURLToString(url);
    }

    private String readURLToString(final URL url) throws IOException, URISyntaxException {
        return IOUtils.toString(new DefaultHttpClient().execute(new HttpGet(url.toURI())).getEntity().getContent());
    }

    private byte[] readURLToByteArray(final URL url) throws IOException, URISyntaxException {
        return IOUtils.toByteArray(new DefaultHttpClient().execute(new HttpGet(url.toURI())).getEntity().getContent());
    }

    public void setCachedStringById(String id, String contents) throws Exception {
        File cacheFile = getCacheFileById(CacheUtils.getHashForString(id));
        FileUtils.writeStringToFile(cacheFile, contents);
    }

    public void setCachedByteArrayById(String id, byte[] contents) throws Exception {
        File cacheFile = getCacheFileById(CacheUtils.getHashForString(id));
        FileUtils.writeByteArrayToFile(cacheFile, contents);
    }

    public Document parseDocumentFromURL(final String htmlURL) throws Exception {
        String indexHtml = getURLCached(htmlURL);
        return Jsoup.parse(indexHtml, htmlURL);
    }
}
