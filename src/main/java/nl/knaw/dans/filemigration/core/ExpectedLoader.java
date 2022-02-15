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
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;

public class ExpectedLoader {
  private static final Logger log = LoggerFactory.getLogger(ExpectedLoader.class);

  private final ExpectedFileDAO expectedDAO;

  public ExpectedLoader(ExpectedFileDAO expectedDAO) {
    this.expectedDAO = expectedDAO;
  }

  public void expectedMigrationFiles(String doi, String[] migrationFiles, FileRights datasetRights) {
    for (String f: migrationFiles) {
      ExpectedFile expectedFile = new ExpectedFile();
      expectedFile.setDoi(doi);
      expectedFile.setSha1_checksum("");
      expectedFile.setEasy_file_id("");
      expectedFile.setFs_rdb_path("");
      expectedFile.setExpected_path("easy-migration/" + f);
      expectedFile.setAdded_during_migration(true);
      expectedFile.setRemoved_thumbnail(false);
      expectedFile.setRemoved_original_directory(false);
      expectedFile.setRemoved_duplicate_file_count(0);
      expectedFile.setTransformed_name(false);
      expectedFile.setVisibleTo(datasetRights.getVisibleTo());
      expectedFile.setAccessibleTo(datasetRights.getAccessibleTo());
      retriedSave(expectedFile);
    }
  }

  public void retriedSave(ExpectedFile expected) {
    try {
      saveExpected(expected);
    } catch(PersistenceException e){
      // logged as error by org.hibernate.engine.jdbc.spi.SqlExceptionHelper
      if (!(e.getCause() instanceof ConstraintViolationException))
        throw e;
      else {
        if (expected.getRemoved_duplicate_file_count() > 10) {
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

  public void saveExpected(ExpectedFile expected) {
      expectedDAO.create(expected);
  }
}
