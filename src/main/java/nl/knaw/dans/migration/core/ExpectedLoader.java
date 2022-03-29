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
import nl.knaw.dans.migration.core.tables.ExpectedDataset;
import nl.knaw.dans.migration.core.tables.ExpectedFile;
import nl.knaw.dans.migration.db.ExpectedDatasetDAO;
import nl.knaw.dans.migration.db.ExpectedFileDAO;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.io.File;
import java.util.Map;

public class ExpectedLoader {
  private static final Logger log = LoggerFactory.getLogger(ExpectedLoader.class);

  private final ExpectedFileDAO expectedFileDAO;
  private final ExpectedDatasetDAO expectedDatasetDAO;
  private final Map<String, String> userToEmail;
  private final Map<String, String> licensesUrlToName;

  public ExpectedLoader(ExpectedFileDAO expectedFileDAO, ExpectedDatasetDAO expectedDatasetDAO, File configDir) {
    this.expectedFileDAO = expectedFileDAO;
    this.expectedDatasetDAO = expectedDatasetDAO;
    this.userToEmail = Mapping.load(new File(configDir + "/easy-users.csv"), "UID", "email");
    this.licensesUrlToName = Mapping.load(new File(configDir + "/licenses.csv"),"url","name");
  }

  public void expectedMigrationFiles(String doi, String[] migrationFiles, FileRights datasetRights) {
    for (String f: migrationFiles) {
      ExpectedFile expectedFile = new ExpectedFile();
      expectedFile.setDoi(doi);
      expectedFile.setSha1Checksum("");
      expectedFile.setEasyFileId("");
      expectedFile.setFsRdbPath("");
      expectedFile.setExpectedPath("easy-migration/" + f);
      expectedFile.setAddedDuringMigration(true);
      expectedFile.setRemovedThumbnail(false);
      expectedFile.setRemovedOriginalDirectory(false);
      expectedFile.setRemovedDuplicateFileCount(0);
      expectedFile.setTransformedName(false);
      expectedFile.setVisibleTo("ANONYMOUS");
      expectedFile.setAccessibleTo("ANONYMOUS");
      retriedSave(expectedFile);
    }
  }

  public void retriedSave(ExpectedFile expected) {
    try {
      saveExpectedFile(expected);
    } catch(PersistenceException e){
      // logged as error by org.hibernate.engine.jdbc.spi.SqlExceptionHelper
      if (!(e.getCause() instanceof ConstraintViolationException))
        throw e;
      else {
        if (expected.getRemovedDuplicateFileCount() > 10) {
          // TODO temporary safe guard?
          log.error("too many retries on duplicate file, skipping: {}", expected);
        }
        else {
          expected.incRemoved_duplicate_file_count();
          retriedSave(expected);
        }
      }
    }
  }

  @UnitOfWork("hibernate")
  public void saveExpectedFile(ExpectedFile expected) {
      expectedFileDAO.create(expected);
  }

  @UnitOfWork("hibernate")
  public void saveExpectedDataset(ExpectedDataset expected) {
    String depositor = expected.getDepositor();
    expected.setDepositor(userToEmail.getOrDefault(depositor, depositor));

    if (null != expected.getLicenseUrl()) {
      String url = expected.getLicenseUrl()
              .replace("https://", "http://")
              .replaceAll("(.html|.txt|.pdf)?/$", "");
      expected.setLicenseName(licensesUrlToName.getOrDefault(url, null));
    }
    log.trace(expected.toString());
    expectedDatasetDAO.create(expected);
  }
}
