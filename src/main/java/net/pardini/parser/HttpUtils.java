package net.pardini.parser;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;


/**
 * Utility class for handling some ill-formed URLs and downloading them using HttpClient.
 */
public class HttpUtils {
// -------------------------- STATIC METHODS --------------------------

    private static URL stringToURL(final String urlString) {
        URL url = null;
        try {
            url = new URL(StringUtils.replace(urlString, " ", "%20"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(String.format("Error parsing URL %s: %s", url, e.getMessage()), e);
        }
        return url;
    }

    public static byte[] readURLToByteArray(final String urlString) throws IOException, URISyntaxException {
        return IOUtils.toByteArray(new DefaultHttpClient().execute(new HttpGet(stringToURL(urlString).toURI())).getEntity().getContent());
    }
}
