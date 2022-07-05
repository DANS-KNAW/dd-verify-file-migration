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

import io.dropwizard.hibernate.UnitOfWork;
import nl.knaw.dans.migration.core.tables.EasyFile;
import nl.knaw.dans.migration.core.tables.ExpectedDataset;
import nl.knaw.dans.migration.core.tables.ExpectedFile;
import nl.knaw.dans.migration.core.tables.InputDataset;
import nl.knaw.dans.migration.db.EasyFileDAO;
import nl.knaw.dans.migration.db.ExpectedDatasetDAO;
import nl.knaw.dans.migration.db.ExpectedFileDAO;
import nl.knaw.dans.migration.db.InputDatasetDAO;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
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

import static nl.knaw.dans.migration.core.HttpHelper.executeReq;

public class EasyFileLoader extends ExpectedLoader {

  private static final Logger log = LoggerFactory.getLogger(EasyFileLoader.class);

  private final EasyFileDAO easyFileDAO;
  private final URI solrUri;
  private final URI fedoraUri;

  /** note: easy-convert-bag-to-deposit does not add emd.xml to bags from the vault */
  private static final String[] migrationFiles = { "provenance.xml", "dataset.xml", "files.xml", "emd.xml" };

  public EasyFileLoader(EasyFileDAO easyFileDAO, ExpectedFileDAO expectedFileDAO, ExpectedDatasetDAO expectedDatasetDAO, InputDatasetDAO inputDatasetDAO, URI solrBaseUri, URI fedoraBaseUri, File configDir) {
    super(expectedFileDAO, expectedDatasetDAO, inputDatasetDAO, configDir);
    this.easyFileDAO = easyFileDAO;
    this.solrUri = solrBaseUri.resolve("datasets/select");
    this.fedoraUri = fedoraBaseUri.resolve("objects/");
  }

  @UnitOfWork("hibernate")
  public void deleteBatch(CSVParser csvRecords, Mode mode, String batch) throws IOException {
    if (mode == Mode.INPUT) {
      inputDatasetDAO.deleteBatch(batch, "fedora");
    } else {
      log.info("start deleting DOIs from {} expected table(s)", mode);
      for (CSVRecord r : csvRecords) {
        FedoraToBagCsv fedoraToBagCsv = new FedoraToBagCsv(r);
        if (fedoraToBagCsv.getComment().contains("OK")) {
          deleteByDoi(fedoraToBagCsv.getDoi(), mode);
        }
      }
      log.info("end deleting DOIs from {} expected table(s)", mode);
    }
  }

  @UnitOfWork("hibernate")
  public void loadFromCsv(FedoraToBagCsv csv, Mode mode, File csvFile) {
    if (mode == Mode.INPUT) {
      inputDatasetDAO.create(new InputDataset(csv, csvFile));
      return;
    }
    if (!csv.getComment().contains("OK"))
      log.warn("skipped {}", csv);
    else try {
      String line = solrInfo(csv.getDatasetId());
      if(line.trim().isEmpty()) {
        log.warn("skipped (not found in solr) {}", csv);
        return;
      }
      SolrFields solrFields = new SolrFields(line);
      DatasetRights datasetRights = solrFields.datasetRights();
      if (mode.doDatasets()) {
        ExpectedDataset expected = datasetRights.expectedDataset();
        if (StringUtils.isNotBlank(csv.getUuid2()))
          expected.setExpectedVersions(2);
        else expected.setExpectedVersions(1);
        expected.setDepositor(solrFields.creator);
        expected.setDoi(csv.getDoi());
        expected.setCitationYear(solrFields.date);
        expected.setDeleted("DELETED".equals(solrFields.state));
        if (!AccessCategory.NO_ACCESS.equals(solrFields.accessCategory)) {
          byte[] emdBytes = readEmd(csv.getDatasetId())
              .getBytes(StandardCharsets.UTF_8);
          String license = MetadataHandler.parse(new ByteArrayInputStream(emdBytes), solrFields.accessCategory).license;
          expected.setLicenseUrl(license);
        }
        saveExpectedDataset(expected);
      }
      if (mode.doFiles()) {
        if (!csv.getComment().contains("no payload")) {
          List<EasyFile> easyFiles = getByDatasetId(csv);
          saveFiles(csv, datasetRights.defaultFileRights, easyFiles);
        }
        expectedMigrationFiles(csv.getDoi(), migrationFiles, "");
      }
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

    public void saveFiles(FedoraToBagCsv csv, FileRights defaultFileRights, List<EasyFile> easyFiles) {
    for (EasyFile f : easyFiles) {
      // note: biggest pdf/image option for europeana in easy-fedora-to-bag does not apply to migration
      log.trace("EasyFile = {}", f);
      final boolean removeOriginal = csv.getTransformation().startsWith("original") && f.getPath().startsWith("original/");
      ExpectedFile expected = new ExpectedFile(csv.getDoi(), f, removeOriginal);
      expected.setDefaultRights(defaultFileRights);
      expected.setAccessibleTo(f.getAccessibleTo());
      expected.setVisibleTo(f.getVisibleTo());
      saveExpectedFile(expected);
    }
  }

  @UnitOfWork("easyBundle")
  public List<EasyFile> getByDatasetId(FedoraToBagCsv csv) {
    return easyFileDAO.findByDatasetId(csv.getDatasetId());
  }
}
