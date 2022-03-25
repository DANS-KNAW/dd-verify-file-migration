/*
 * Copyright (C) 2021 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.migration.core;

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
        if (statusCode == 410) {
            if (logNotFound)
                log.error("Deactivated {}", req.getURI());
            return "";
        }
        else if (statusCode < 200 || statusCode >= 300)
            throw new IOException("not expected response code: " + statusCode);
        else
            return EntityUtils.toString(resp.getEntity()); // max size 2147483647L
    }

}
