package nl.knaw.dans.filemigration.core;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HttpHelper {
    private static final HttpClient client = HttpClients.createDefault();
    private static final Logger log = LoggerFactory.getLogger(HttpHelper.class);

    public static String executeReq(HttpGet req, boolean logNotFound) throws IOException {
        log.info("{}", req);
        HttpResponse resp = client.execute(req);
        int statusCode = resp.getStatusLine().getStatusCode();
        if (statusCode == 404) {
            if (logNotFound)
                log.error("Could not find {}", req.getURI());
            return "";
        }
        else if (statusCode < 200 || statusCode >= 300)
            throw new IOException("not expected response code: " + statusCode);
        else
            return EntityUtils.toString(resp.getEntity()); // max size 2147483647L
    }

}
