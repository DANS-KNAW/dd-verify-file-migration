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

import nl.knaw.dans.migration.core.tables.ExpectedDataset;
import nl.knaw.dans.migration.core.tables.ExpectedFile;
import nl.knaw.dans.migration.db.ExpectedDatasetDAO;
import nl.knaw.dans.migration.db.ExpectedFileDAO;
import nl.knaw.dans.migration.db.InputDatasetDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

public class ExpectedLoader {
  private static final Logger log = LoggerFactory.getLogger(ExpectedLoader.class);

  private final ExpectedFileDAO expectedFileDAO;
  private final ExpectedDatasetDAO expectedDatasetDAO;
  final InputDatasetDAO inputDatasetDAO;
  private final Map<String, String> userToEmail;
  private final Map<String, String> licensesUrlToName;

  public ExpectedLoader(ExpectedFileDAO expectedFileDAO, ExpectedDatasetDAO expectedDatasetDAO, InputDatasetDAO inputDatasetDAO, File configDir) {
    this.expectedFileDAO = expectedFileDAO;
    this.expectedDatasetDAO = expectedDatasetDAO;
    this.inputDatasetDAO = inputDatasetDAO;
    this.userToEmail = Mapping.load(new File(configDir + "/easy-users.csv"), true, "UID", "email");
    this.licensesUrlToName = Mapping.load(new File(configDir + "/licenses.csv"), false, "url","name");
  }

  public void deleteByDoi(String doi, Mode mode) {
    if (mode.doFiles())
      expectedFileDAO.deleteByDoi(doi);
    if (mode.doDatasets())
      expectedDatasetDAO.deleteByDoi(doi);
  }

  public void expectedMigrationFiles(String doi, String[] migrationFiles, String easyFileId) {
    for (String f: migrationFiles) {
      ExpectedFile expectedFile = new ExpectedFile();
      expectedFile.setDoi(doi);
      expectedFile.setSha1Checksum("");
      expectedFile.setEasyFileId(easyFileId);
      expectedFile.setFsRdbPath("");
      expectedFile.setExpectedPath("easy-migration/" + f);
      expectedFile.setAddedDuringMigration(true);
      expectedFile.setRemovedThumbnail(false);
      expectedFile.setRemovedOriginalDirectory(false);
      expectedFile.setTransformedName(false);
      expectedFile.setVisibleTo("ANONYMOUS");
      expectedFile.setAccessibleTo("ANONYMOUS");
      saveExpectedFile(expectedFile);
    }
  }

  public void saveExpectedFile(ExpectedFile expected) {
      expectedFileDAO.create(expected);
  }

  public void saveExpectedDataset(ExpectedDataset expected) {
    String depositor = expected.getDepositor();
    expected.setDepositor(userToEmail.getOrDefault(depositor.toLowerCase(), depositor));

    if (null != expected.getLicenseUrl())
      expected.setLicenseName(licensesUrlToName.getOrDefault(expected.getLicenseUrl(), null));
    expectedDatasetDAO.create(expected);
  }
}
