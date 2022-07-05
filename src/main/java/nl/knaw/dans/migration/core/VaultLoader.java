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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.dropwizard.hibernate.UnitOfWork;
import nl.knaw.dans.lib.dataverse.DataverseItemDeserializer;
import nl.knaw.dans.lib.dataverse.MetadataFieldDeserializer;
import nl.knaw.dans.lib.dataverse.ResultItemDeserializer;
import nl.knaw.dans.lib.dataverse.model.dataset.MetadataField;
import nl.knaw.dans.lib.dataverse.model.dataverse.DataverseItem;
import nl.knaw.dans.lib.dataverse.model.search.ResultItem;
import nl.knaw.dans.migration.core.MetadataHandler.DatasetMetadata;
import nl.knaw.dans.migration.core.tables.ExpectedDataset;
import nl.knaw.dans.migration.core.tables.ExpectedFile;
import nl.knaw.dans.migration.core.tables.InputDataset;
import nl.knaw.dans.migration.db.ExpectedDatasetDAO;
import nl.knaw.dans.migration.db.ExpectedFileDAO;
import nl.knaw.dans.migration.db.InputDatasetDAO;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static nl.knaw.dans.migration.core.HttpHelper.executeReq;

public class VaultLoader extends ExpectedLoader {

  private static final Logger log = LoggerFactory.getLogger(VaultLoader.class);

  private final URI bagStoreBagsUri;
  private final URI bagIndexBagsUri;
  private final URI bagIndexSeqUri;
  private final ObjectMapper mapper;

  public VaultLoader(ExpectedFileDAO expectedFileDAO, ExpectedDatasetDAO expectedDatasetDAO, InputDatasetDAO inputDatasetDAO, URI bagStoreBaseUri, URI bagIndexBaseUri, File configDir) {
    super(expectedFileDAO, expectedDatasetDAO, inputDatasetDAO, configDir);
    bagIndexSeqUri = bagIndexBaseUri.resolve("bag-sequence");
    bagIndexBagsUri = bagIndexBaseUri.resolve("bags/");
    bagStoreBagsUri = bagStoreBaseUri.resolve("bags/");


    mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addDeserializer(MetadataField.class, new MetadataFieldDeserializer());
    module.addDeserializer(DataverseItem.class, new DataverseItemDeserializer());
    module.addDeserializer(ResultItem.class, new ResultItemDeserializer(mapper));
    mapper.registerModule(module);
  }

  @UnitOfWork("hibernate")
  public void deleteBatch(String batch, String bagStore) {
    inputDatasetDAO.deleteBatch(batch,bagStore);
  }

  @UnitOfWork("hibernate")
  public void loadFromVault(UUID uuid, Mode mode, String batch, String bagStore) {
    if (mode == Mode.INPUT) {
      inputDatasetDAO.create(new InputDataset(uuid, "OK", batch, bagStore));
      return;
    }
    BagInfo bagInfo;
    try {
      bagInfo = bagInfoFromIndex(uuid.toString());
    } catch ( Exception e) {
      return;
    }
    log.trace("from input {}", bagInfo);
    if (bagInfo.getBagId() != null && bagInfo.getBagId().equals(bagInfo.getBaseId())) {
      log.trace("Processing {}", bagInfo);
      deleteByDoi(bagInfo.getDoi(), mode);
      String[] bagSeq = readBagSequence(uuid);
      List<BagInfo> bagInfos= StreamSupport
          .stream(Arrays.stream(bagSeq).spliterator(), false)
          .map(this::bagInfoFromIndex)
          .sorted(new BagInfoComparator()).collect(Collectors.toList());

      ExpectedDataset firstExpectedDataset = processBag(uuid.toString(), 0, bagInfo.getDoi(), mode);

      // rest of the bags from a sequence
      ExpectedDataset expectedDataset = null;
      for (int bagSeNr = 1; bagSeNr < bagInfos.size(); bagSeNr++) {
        BagInfo info = bagInfos.get(bagSeNr);
        log.trace("{} from sequence {}", bagSeNr, info);
        expectedDataset = processBag(info.getBagId(), bagSeNr, bagInfos.get(0).getDoi(), mode);
      }

      // save (a mix of) the first and/or last dataset
      if (expectedDataset == null)
        expectedDataset = firstExpectedDataset;
      else
        expectedDataset.setCitationYear(firstExpectedDataset.getCitationYear());
      if (mode.doDatasets()) {
        expectedDataset.setDepositor(readDepositor(uuid.toString()));
        expectedDataset.setDoi(bagInfo.getDoi());
        expectedDataset.setExpectedVersions(bagSeq.length);
        saveExpectedDataset(expectedDataset);
      }
    }
  }

  /** note: easy-convert-bag-to-deposit does not add emd.xml to bags from the vault */
  private static final String[] migrationFiles = { "provenance.xml", "dataset.xml", "files.xml" };

  /** @return either deleted=true or accessCategory, embargoDate and license */
  public ExpectedDataset processBag(String uuid, int bagSeqNr, String baseDoi, Mode mode) {
    byte[] ddmBytes = readBagFile(uuid, "metadata/", "dataset.xml")
        .getBytes(StandardCharsets.UTF_8);// parsed twice to reuse code shared with EasyFileLoader
    ExpectedDataset expectedDataset;
    if (ddmBytes.length == 0) {
      // presuming deactivated, logging shows whether it was indeed deactivated or not found
      expectedDataset = new ExpectedDataset();
      expectedDataset.setDeleted(true);
    } else {
      DatasetRights datasetRights = DatasetRightsHandler.parseRights(new ByteArrayInputStream(ddmBytes));
      expectedDataset = datasetRights.expectedDataset();
      DatasetMetadata metadata = MetadataHandler.parse(new ByteArrayInputStream(ddmBytes), datasetRights.accessCategory);
      expectedDataset.setCitationYear(metadata.created.substring(0, 4));
      expectedDataset.setLicenseUrl(metadata.license);
      if (mode.doFiles()) {
        byte[] xmlBytes = readBagFile(uuid, "metadata/", "files.xml").getBytes(StandardCharsets.UTF_8);
        Map<String, FileRights> filesXml = FileRightsHandler.parseRights(new ByteArrayInputStream(xmlBytes));
        readManifest(uuid).forEach(m ->
            createExpectedFile(baseDoi, bagSeqNr, m, filesXml, datasetRights.defaultFileRights)
        );
        expectedMigrationFiles(baseDoi, migrationFiles, String.valueOf(bagSeqNr));
      }
    }
    return expectedDataset;
  }

  private void createExpectedFile(String doi, int bagSeqNr, ManifestCsv m, Map<String, FileRights> fileRightsMap, FileRights defaultFileRights) {
    String path = m.getPath();
    FileRights fileRights = fileRightsMap.get(path).applyDefaults(defaultFileRights);
    log.trace("{} {}", path, fileRights);
    ExpectedFile expectedFile = new ExpectedFile(doi, m, fileRights);
    expectedFile.setEasyFileId(String.valueOf(bagSeqNr));
    expectedFile.setExpectedPath(expectedFile.getExpectedPath().replaceAll("^data/",""));
    saveExpectedFile(expectedFile);
  }

  private Stream<ManifestCsv> readManifest(String uuid) {
    try {
      return ManifestCsv.parse(readBagFile(uuid,"manifest-sha1.txt"));
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

    private String readDepositor(String uuid) {
      String[] bagInfoLines = (readBagFile(uuid, "bag-info.txt"))
          .split(System.lineSeparator());
      Optional<String> account = Arrays.stream(bagInfoLines)
              .filter(l -> l.startsWith("EASY-User-Account"))
              .map(l -> l.replaceAll(".*:","").trim())
              .findFirst();
      if (!account.isPresent())
        throw new IllegalStateException("No EASY-User-Account in bag-info.txt of "+ uuid);
      return account.get();
  }

  private BagInfo bagInfoFromIndex(String uuid) {
    URI uri = bagIndexBagsUri.resolve(uuid);
    try {
      String s = executeReq(new HttpGet(uri), true);
      if ("".equals(s)) return new BagInfo(); // not found
      else try {
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
    URIBuilder builder = new URIBuilder(bagIndexSeqUri)
        .setParameter("contains", uuid.toString());
    try {
      return executeReq(new HttpGet(builder.build()), false).split(System.lineSeparator());
    }
    catch (IOException | URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  private String readBagFile(String uuid, String... pathSegments) {
    log.trace("reading bag file {} {}",pathSegments.length,pathSegments);
    if (pathSegments.length < 1) throw new NotImplementedException("too few path segments. Minimal 1, actual "+ pathSegments.length);
    URI initialUri = bagStoreBagsUri
        .resolve(uuid + "/")
        .resolve(pathSegments[0]);

    // TODO loop, recursion or reduce? For now keep it simple. https://www.tabnine.com/code/java/methods/feign.RequestTemplate/resolve
    if (pathSegments.length > 2) throw new NotImplementedException("too many path segments maximum 2, actual "+ pathSegments.length);
    URI uri = pathSegments.length == 1 ? initialUri : initialUri.resolve(pathSegments[1]);

    URIBuilder builder = new URIBuilder(uri).setCustomQuery("forceInactive");
    try {
      return executeReq(new HttpGet(builder.build()), false);
    }
    catch (IOException | URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}
