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

import nl.knaw.dans.migration.api.EasyFile;
import nl.knaw.dans.migration.api.ExpectedFile;
import nl.knaw.dans.migration.db.EasyFileDAO;
import nl.knaw.dans.migration.db.ExpectedFileDAO;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static nl.knaw.dans.migration.core.HttpHelper.executeReq;

public class EasyFileLoader extends ExpectedLoader {

  private static final Logger log = LoggerFactory.getLogger(EasyFileLoader.class);

  private final EasyFileDAO easyFileDAO;
  private final URI solrUri;

  public EasyFileLoader(EasyFileDAO easyFileDAO, ExpectedFileDAO expectedDAO, URI solrBaseUri) {
    super(expectedDAO);
    this.easyFileDAO = easyFileDAO;
    this.solrUri = solrBaseUri.resolve("datasets/select");
  }

  public void loadFromCsv(FedoraToBagCsv csv) {
    if (!csv.getComment().contains("OK"))
      log.warn("skipped {}", csv);
    else {
      // read fedora files before adding expected migration files
      // thus we don't write anything when reading fails
      FileRights datasetRights = getDatasetRights(csv.getDatasetId());
      if (!csv.getComment().contains("no payload"))
        fedoraFiles(csv, datasetRights.getEmbargoDate());
      expectedMigrationFiles(csv.getDoi(), migrationFiles, datasetRights);
    }
  }

  private static final String REQUESTED_FIELDS = "emd_date_available_formatted,dc_rights";

  @NotNull
  private FileRights getDatasetRights(String datasetId) {
    try {
      String line = rightsFromSolr(datasetId);
      log.trace(line);
      CSVRecord record = CSVParser.parse(
              new ByteArrayInputStream(line.getBytes(StandardCharsets.UTF_8)),
              StandardCharsets.UTF_8,
              CSVFormat.RFC4180.withDelimiter(',')
      ).getRecords().get(0);
      String dateAvailable = record.get(0);
      String[] dcRights = record.get(1)
              .replaceAll("^\"","") // strip leading quote
              .replaceAll("\"$","") // strip trailing quote
              .split(", *");
      Optional<DatasetRights> maybeRights= Arrays.stream(dcRights)
              .filter(this::isDatasetRights)
              .map(DatasetRights::valueOf)
              .findFirst();
      FileRights fileRights = new FileRights();
      fileRights.setEmbargoDate(dateAvailable);
      if (maybeRights.isPresent())
        fileRights.setFileRights(maybeRights.get());
      else {
        log.warn("no dataset rights found in solr response: {} using NO_ACCESS", line);
        fileRights.setFileRights(DatasetRights.NO_ACCESS);
      }
      return fileRights;
    } catch (IOException | URISyntaxException e) {
      // expecting an empty line when not found, other errors are fatal
      throw new IllegalStateException(e.getMessage(), e);
    }
  }

  private boolean isDatasetRights(String s) {
    try {
      DatasetRights.valueOf(s);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  protected String rightsFromSolr(String datasetId) throws IOException, URISyntaxException {
    URIBuilder builder = new URIBuilder(solrUri)
            .setParameter("q", "sid:\""+ datasetId +"\"")
            .setParameter("fl", REQUESTED_FIELDS)
            .setParameter("wt", "csv")
            .setParameter("csv.header", "false")
            .setParameter("version", "2.2");
    return executeReq(new HttpGet(builder.build()), false);
  }

  /** note: easy-convert-bag-to-deposit does not add emd.xml to bags from the vault */
  private static final String[] migrationFiles = { "provenance.xml", "dataset.xml", "files.xml", "emd.xml" };

  /**
   * @param csv         not null, record produced by easy-fedora-to-bag
   * @param embargoDate null if data-available not in the future
   */
  private void fedoraFiles(FedoraToBagCsv csv, String embargoDate) {
    log.trace(csv.toString());
    List<EasyFile> easyFiles = getByDatasetId(csv);
    for (EasyFile f : easyFiles) {
      // note: biggest pdf/image option for europeana in easy-fedora-to-bag does not apply to migration
      log.trace("EasyFile = {}", f);
      final boolean removeOriginal = csv.getTransformation().startsWith("original") && f.getPath().startsWith("original/");
      ExpectedFile expected = new ExpectedFile(csv.getDoi(), f, removeOriginal);
      expected.setAccessibleTo(f.getAccessible_to());
      expected.setVisibleTo(f.getVisible_to());
      expected.setEmbargo_date(embargoDate);
      retriedSave(expected);
    }
  }

  public List<EasyFile> getByDatasetId(FedoraToBagCsv csv) {
    return easyFileDAO.findByDatasetId(csv.getDatasetId());
  }
}
