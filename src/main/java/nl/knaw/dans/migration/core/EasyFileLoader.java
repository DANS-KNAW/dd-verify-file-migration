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

import nl.knaw.dans.migration.core.tables.EasyFile;
import nl.knaw.dans.migration.core.tables.ExpectedDataset;
import nl.knaw.dans.migration.core.tables.ExpectedFile;
import nl.knaw.dans.migration.db.EasyFileDAO;
import nl.knaw.dans.migration.db.ExpectedDatasetDAO;
import nl.knaw.dans.migration.db.ExpectedFileDAO;
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
import java.util.List;

import static nl.knaw.dans.migration.core.DatasetLicenseHandler.parseLicense;
import static nl.knaw.dans.migration.core.HttpHelper.executeReq;

public class EasyFileLoader extends ExpectedLoader {

  private static final Logger log = LoggerFactory.getLogger(EasyFileLoader.class);

  private final EasyFileDAO easyFileDAO;
  private final URI solrUri;
  private URI fedoraUri;

  /** note: easy-convert-bag-to-deposit does not add emd.xml to bags from the vault */
  private static final String[] migrationFiles = { "provenance.xml", "dataset.xml", "files.xml", "emd.xml" };

  public EasyFileLoader(EasyFileDAO easyFileDAO, ExpectedFileDAO expectedFileDAO, ExpectedDatasetDAO expectedDatasetDAO, URI solrBaseUri, URI fedoraBaseUri, File configDir) {
    super(expectedFileDAO, expectedDatasetDAO, configDir);
    this.easyFileDAO = easyFileDAO;
    this.solrUri = solrBaseUri.resolve("datasets/select");
    this.fedoraUri = fedoraBaseUri.resolve("objects/");
  }

  public void loadFromCsv(FedoraToBagCsv csv) {
    if (!csv.getComment().contains("OK"))
      log.warn("skipped {}", csv);
    else try {
      SolrFields solrFields = new SolrFields(solrInfo(csv.getDatasetId()));
      DatasetRights datasetRights = solrFields.datasetRights();
      ExpectedDataset expected = datasetRights.expectedDataset(solrFields.creator);
      expected.setDoi(csv.getDoi());
      expected.setCitationYear(solrFields.date);
      expected.setDeleted("DELETED".equals(solrFields.state));
      if (!AccessCategory.NO_ACCESS.equals(solrFields.accessCategory)) {
        byte[] emdBytes = readEmd(csv.getDatasetId())
            .getBytes(StandardCharsets.UTF_8);
        String license = parseLicense(new ByteArrayInputStream(emdBytes), solrFields.accessCategory);
       expected.setLicenseUrl(license);
      }
      // so far we collected dataset metadata, we will store it into the DB as the very last action
      // thus we don't write anything when reading something fails
      if (!csv.getComment().contains("no payload")) {
        fedoraFiles(csv, datasetRights.defaultFileRights);
      }
      expectedMigrationFiles(csv.getDoi(), migrationFiles, datasetRights.defaultFileRights);
      log.trace("solr.emd_date_created_formatted: " + solrFields.date.substring(0,4));
      saveExpectedDataset(expected);
    } catch (IOException | URISyntaxException e) {
      // expecting an empty line when not found, other errors are fatal
      throw new IllegalStateException(e.getMessage(), e);
    }
  }

  protected String solrInfo(String datasetId) throws IOException, URISyntaxException {
    URIBuilder builder = new URIBuilder(solrUri)
            .setParameter("q", "sid:\""+ datasetId +"\"")
            .setParameter("fl", SolrFields.requestedFields)
            .setParameter("wt", "csv")
            .setParameter("csv.header", "false")
            .setParameter("version", "2.2");
    return executeReq(new HttpGet(builder.build()), false);
  }

  protected String readEmd(String datasetId) throws IOException, URISyntaxException {
    // the colon in datasetId spoils URI.resolve
    URI uri = URI.create(fedoraUri + datasetId + "/datastreams/EMD/content");
    return executeReq(new HttpGet(new URIBuilder(uri).build()), false);
  }

  /**
   * @param csv record produced by easy-fedora-to-bag
   */
  private void fedoraFiles(FedoraToBagCsv csv, FileRights defaultFileRights) {
    log.trace(csv.toString());
    List<EasyFile> easyFiles = getByDatasetId(csv);
    for (EasyFile f : easyFiles) {
      // note: biggest pdf/image option for europeana in easy-fedora-to-bag does not apply to migration
      log.trace("EasyFile = {}", f);
      final boolean removeOriginal = csv.getTransformation().startsWith("original") && f.getPath().startsWith("original/");
      ExpectedFile expected = new ExpectedFile(csv.getDoi(), f, removeOriginal);
      expected.setDefaultRights(defaultFileRights);
      expected.setAccessibleTo(f.getAccessibleTo());
      expected.setVisibleTo(f.getVisibleTo());
      retriedSave(expected);
    }
  }

  public List<EasyFile> getByDatasetId(FedoraToBagCsv csv) {
    return easyFileDAO.findByDatasetId(csv.getDatasetId());
  }
}
