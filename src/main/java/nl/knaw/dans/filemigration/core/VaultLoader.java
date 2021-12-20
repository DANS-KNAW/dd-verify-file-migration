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
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.hibernate.cfg.NotYetImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;

public class VaultLoader {

  private static final Logger log = LoggerFactory.getLogger(VaultLoader.class);

  private final ExpectedFileDAO expectedDAO;
  private final URI bagstoreBaseUri;
  private final HttpClient client = HttpClients.createDefault();

  public VaultLoader(ExpectedFileDAO expectedDAO, URI bagstoreBaseUri) {
    this.expectedDAO = expectedDAO;
    this.bagstoreBaseUri = bagstoreBaseUri;
  }

  public void saveExpected(ExpectedFile expected) {
    expectedDAO.create(expected);
  }

  public void loadFromVault(UUID uuid) {
    for(CSVRecord r: readManifest(uuid)) {
      ManifestCsv m = new ManifestCsv(r);
      log.trace("{} {}", m.getSha1(), m.getPath());
    }
    throw new NotYetImplementedException();
  }

  private CSVParser readManifest(UUID uuid) {
    URI uri = bagstoreBaseUri.resolve("bags/").resolve(uuid.toString() + "/").resolve("manifest-sha1.txt");
    log.info("Reading {}", uri);
    try {
      HttpResponse r = client.execute(new HttpGet(uri));
      int statusCode = r.getStatusLine().getStatusCode();
      if (statusCode == 404) {
        log.error("Could not find manifest of {}", uuid);
        return ManifestCsv.parse(new ByteArrayInputStream(new byte[0]));
      }
      else if (statusCode < 200 || statusCode >= 300)
        throw new IOException("not expected response code: " + statusCode);
      else {
        return ManifestCsv.parse(r.getEntity().getContent());
      }
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
