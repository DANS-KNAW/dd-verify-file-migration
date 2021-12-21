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
package nl.knaw.dans.filemigration.core;

import nl.knaw.dans.filemigration.api.ExpectedFile;
import nl.knaw.dans.filemigration.db.ExpectedFileDAO;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.hibernate.cfg.NotYetImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import java.util.stream.Stream;

public class VaultLoader {

  private static final Logger log = LoggerFactory.getLogger(VaultLoader.class);

  private final ExpectedFileDAO expectedDAO;
  private final URI bagStoreBaseUri;
  private final URI bagIndexBaseUri;
  private final HttpClient client = HttpClients.createDefault();

  public VaultLoader(ExpectedFileDAO expectedDAO, URI bagStoreBaseUri, URI bagIndexBaseUri) {
    this.expectedDAO = expectedDAO;
    this.bagStoreBaseUri = bagStoreBaseUri;
    this.bagIndexBaseUri = bagIndexBaseUri;
  }

  public void saveExpected(ExpectedFile expected) {
    expectedDAO.create(expected);
  }

  public void loadFromVault(UUID uuid) {
    readManifest(uuid).forEach(this::toExpected);
    log.trace(readBagInfo(uuid));
    log.trace(readBagSequence(uuid));
    throw new NotYetImplementedException();
  }

  private void toExpected(ManifestCsv m) {
    log.trace("{} {}", m.getSha1(), m.getPath());
  }

  private Stream<ManifestCsv> readManifest(UUID uuid) {
    URI uri = bagStoreBaseUri
        .resolve("bags/")
        .resolve(uuid.toString()+"/")
        .resolve("manifest-sha1.txt");
    try {
      String s = getRequest(uri, true)
          .replaceAll(" *\n *","\n")
          .replaceAll("[ \t]+","\t");
      return ManifestCsv.parse(s);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String readBagInfo(UUID uuid) {
    URI uri = bagIndexBaseUri
        .resolve("bags/")
        .resolve(uuid.toString());
    try {
      return getRequest(uri, true);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  private String readBagSequence(UUID uuid) {
    URI uri = bagIndexBaseUri
        .resolve("bag-sequence/")
        .resolve(uuid.toString());
    try {
      return getRequest(uri, false);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String getRequest(URI uri, boolean logNotFound) throws IOException {
    log.info("Reading {}", uri);
    HttpResponse r = client.execute(new HttpGet(uri));
    int statusCode = r.getStatusLine().getStatusCode();
    if (statusCode == 404) {
      if (logNotFound)
        log.error("Could not find {}", uri);
      return "";
    }
    else if (statusCode < 200 || statusCode >= 300)
      throw new IOException("not expected response code: " + statusCode);
    else
      return EntityUtils.toString(r.getEntity());
  }
}
