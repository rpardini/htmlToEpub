package net.pardini.parser;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Pardini
 * Date: 16/09/12
 * Time: 00:31
 * To change this template use File | Settings | File Templates.
 */
public class CacheUtils {

    public static String getHashForString(final String id) {
        return DigestUtils.md5Hex(id);
    }

    public static String getIDForImage(final String src) {
        return String.format("%s.jpg", CacheUtils.getHashForString(src));
    }

}
