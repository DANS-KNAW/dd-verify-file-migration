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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import nl.knaw.dans.filemigration.api.ExpectedFile;
import nl.knaw.dans.filemigration.db.ExpectedFileDAO;
import nl.knaw.dans.lib.dataverse.DataverseItemDeserializer;
import nl.knaw.dans.lib.dataverse.MetadataFieldDeserializer;
import nl.knaw.dans.lib.dataverse.ResultItemDeserializer;
import nl.knaw.dans.lib.dataverse.model.dataset.MetadataField;
import nl.knaw.dans.lib.dataverse.model.dataverse.DataverseItem;
import nl.knaw.dans.lib.dataverse.model.search.ResultItem;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.stream.Stream;

public class VaultLoader {

  private static final Logger log = LoggerFactory.getLogger(VaultLoader.class);

  private final ExpectedFileDAO expectedDAO;
  private final URI bagStoreBaseUri;
  private final URI bagIndexBaseUri;
  private final URI bagSeqUri;
  private final HttpClient client = HttpClients.createDefault();
  private final ObjectMapper mapper;

  public VaultLoader(ExpectedFileDAO expectedDAO, URI bagStoreBaseUri, URI bagIndexBaseUri) {
    this.expectedDAO = expectedDAO;
    bagSeqUri = bagIndexBaseUri.resolve("bag-sequence");
    this.bagStoreBaseUri = bagStoreBaseUri;
    this.bagIndexBaseUri = bagIndexBaseUri;

    mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addDeserializer(MetadataField.class, new MetadataFieldDeserializer());
    module.addDeserializer(DataverseItem.class, new DataverseItemDeserializer());
    module.addDeserializer(ResultItem.class, new ResultItemDeserializer(mapper));
    mapper.registerModule(module);
  }

  public void saveExpected(ExpectedFile expected) {
    expectedDAO.create(expected);
  }

  public void loadFromVault(UUID uuid) {
    BagInfo bagInfo = readBagInfo(uuid);
    if (!bagInfo.getBagId().equals(bagInfo.getBaseId()))
      log.info("Skipping {}, it is another version of {}", uuid, bagInfo.getBaseId());
    else {
      log.trace("Processing {}", bagInfo);
      String[] bagSeq = readBagSequence(uuid);
      if (bagSeq.length == 0)
        readManifest(uuid.toString()).forEach(this::toExpected);
      else
        for (String uuidInSeq : bagSeq) {
          readManifest(uuidInSeq).forEach(this::toExpected);
        }
    }
  }

  private void toExpected(ManifestCsv m) {
    log.trace("{} {}", m.getSha1(), m.getPath());
  }

  private Stream<ManifestCsv> readManifest(String uuid) {
    URI uri = bagStoreBaseUri
        .resolve("bags/")
        .resolve(uuid+"/")
        .resolve("manifest-sha1.txt"); // TODO in next iteration a variant for metadata/files.xml
    try {
      String s = executeReq(new HttpGet(uri), true)
          .replaceAll(" *\n *", "\n")
          .replaceAll("(?m)^([0-9a-zA-Z]+)[ \t]+","$1\t");
      return ManifestCsv.parse(s);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private BagInfo readBagInfo(UUID uuid) {
    URI uri = bagIndexBaseUri
        .resolve("bags/")
        .resolve(uuid.toString());
    try {
      String s = executeReq(new HttpGet(uri), true);
      try {
        return mapper.readValue(s, BagInfoEnvelope.class).getResult().getBagInfo();
      }
      catch (JsonProcessingException e) {
        log.error("Could not parse BagInfo of {} reason {} content {}", uuid, e.getMessage(), s);
        return new BagInfo();
      }
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  private String[] readBagSequence(UUID uuid) {
    URIBuilder builder = new URIBuilder(bagSeqUri)
        .setParameter("contains", uuid.toString());
    try {
      return executeReq(new HttpGet(builder.build()), false).split(System.lineSeparator());
    }
    catch (IOException | URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  private String executeReq(HttpGet req, boolean logNotFound) throws IOException {
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
      return EntityUtils.toString(resp.getEntity());
  }
}
