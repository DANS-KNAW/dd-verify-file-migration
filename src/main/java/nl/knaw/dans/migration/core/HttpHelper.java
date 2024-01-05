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

import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HttpHelper {
    private static final HttpClient client = HttpClients.createDefault();
    private static final Logger log = LoggerFactory.getLogger(HttpHelper.class);

    public static String executeReq(HttpGet req, boolean logNotFound) throws IOException {
        log.info("{}", req);
        BasicHttpClientResponseHandler handler = new BasicHttpClientResponseHandler();
        try {
            return client.execute(req, handler);
        }
        catch (HttpResponseException e) {
            int statusCode = e.getStatusCode();
            if (statusCode == 404) {
                if (logNotFound)
                    log.error("Could not find {}", req.getRequestUri());
                return "";
            }
            if (statusCode == 410) {
                if (logNotFound)
                    log.error("Deactivated {}", req.getRequestUri());
                return "";
            }
            else if (statusCode < 200 || statusCode >= 300)
                throw new IOException("not expected response code: " + statusCode);
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
