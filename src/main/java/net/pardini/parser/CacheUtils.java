package net.pardini.parser;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;


/**
 * The good old full-of-static-methods utility class.
 * Here we handle caching of retrieved resources from the web, so that we don't hammer the originating website too much.
 * getIDForImage is also important because it's the key for referencing images in the generated ePubs.
 */
public class CacheUtils {
// ------------------------------ FIELDS ------------------------------

    private final static Logger log = LoggerFactory.getLogger(CacheUtils.class);
    private final static Charset utf8 = Charset.forName("UTF8");

// -------------------------- STATIC METHODS --------------------------

    public static String getIDForImage(final String src) {
        return String.format("%s.jpg", CacheUtils.getHashForString(src));
    }

    private static String getHashForString(final String id) {
        return DigestUtils.md5Hex(id);
    }

    public static String getURLCached(String urlString) throws Exception {
        return new String(getURLCachedByteArray(urlString), utf8);
    }

    public static byte[] getURLCachedByteArray(String urlString) throws Exception {
        assert urlString != null;

        byte[] content;
        content = getCachedByteArrayById(urlString);
        if (content != null) {
            log.debug(String.format("Hit cache for '%s'", urlString));
            return content;
        }

        log.warn(String.format("Cache miss for '%s'", urlString));

        content = HttpUtils.readURLToByteArray(urlString);
        setCachedByteArrayById(urlString, content);
        return content;
    }

    private static byte[] getCachedByteArrayById(String id) throws Exception {
        File cacheFile = getCacheFileById(CacheUtils.getHashForString(id));
        if (!cacheFile.exists()) {
            log.debug(String.format("Cache file %s does not exist, cache nil.", cacheFile.toString()));
            return null;
        }
        return IOUtils.toByteArray(cacheFile.toURI().toURL().openStream());
    }

    private static File getCacheFileById(final String id) {
        File tempDir = new File("C:\\temp\\caches");
        if (!tempDir.exists()) {
            if (!tempDir.mkdirs()) throw new RuntimeException("Unable to create temp dir.");
        }
        String fullFileName = String.format("%s%s%s.html", tempDir.getAbsolutePath(), File.separator, id);
        log.debug(String.format("Cache filename for '%s' is '%s'", id, fullFileName));
        return new File(fullFileName);
    }

    private static void setCachedByteArrayById(String id, byte[] contents) throws Exception {
        File cacheFile = getCacheFileById(CacheUtils.getHashForString(id));
        FileUtils.writeByteArrayToFile(cacheFile, contents);
    }
}
