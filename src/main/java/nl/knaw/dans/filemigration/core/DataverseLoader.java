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

import nl.knaw.dans.filemigration.api.ActualFile;
import nl.knaw.dans.filemigration.api.ExpectedFile;
import nl.knaw.dans.filemigration.db.ActualFileDAO;
import nl.knaw.dans.lib.dataverse.DataverseClient;
import nl.knaw.dans.lib.dataverse.DataverseResponse;
import nl.knaw.dans.lib.dataverse.model.dataset.DatasetVersion;
import nl.knaw.dans.lib.dataverse.model.file.FileMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.util.Iterator;
import java.util.List;

public class DataverseLoader {
  private static final Logger log = LoggerFactory.getLogger(DataverseLoader.class);

  private final ActualFileDAO actualFileDAO;
  private final DataverseClient client;

  public DataverseLoader(DataverseClient client, ActualFileDAO actualFileDAO) {
    this.actualFileDAO = actualFileDAO;
    this.client = client;
  }

  public void saveActual(ActualFile actual) {
      actualFileDAO.create(actual);
  }

  public void loadFromDataset(String doi) {
    log.info("Loading DOI {}", doi);
    List<DatasetVersion> versions;
    try {
      versions = client.dataset(doi).getAllVersions().getData();
    } catch (Exception e) {
      log.error("Could not retrieve file metas for DOI: {}", doi, e);
      throw new RuntimeException(e);
    }
    versions.sort(new DatasetVersionComparator());
    int seqNum = 1;
    for (DatasetVersion v : versions) {
      int count = 0;
      for (FileMeta f : v.getFiles()) {
        ++count;
        log.debug("Stored file, label: {}, directoryLabel: {}", f.getLabel(), f.getDirectoryLabel());
      }
      log.info("Stored {} basic file metas for DOI {}, Version seqNr {}", count, doi, seqNum);
    }
  }
}
