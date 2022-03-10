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

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import nl.knaw.dans.migration.api.ActualFile;
import nl.knaw.dans.migration.db.ActualFileDAO;
import nl.knaw.dans.lib.dataverse.DataverseClient;
import nl.knaw.dans.lib.dataverse.model.dataset.DatasetVersion;
import nl.knaw.dans.lib.dataverse.model.file.DataFile;
import nl.knaw.dans.lib.dataverse.model.file.Embargo;
import nl.knaw.dans.lib.dataverse.model.file.FileMeta;
import org.hsqldb.lib.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        log.debug(actual.toString());
        actualFileDAO.create(actual);
    }

    public void loadFromDataset(String doi) {
        if (StringUtil.isEmpty(doi))
            return; // workaround
        log.info("Reading {} from dataverse", doi);
        List<DatasetVersion> versions;
        try {
            versions = client.dataset(doi).getAllVersions().getData();
        }
        catch (UnrecognizedPropertyException e) {
            log.error("Skipping {} {}", doi, e.getMessage());
            return;
        }
        catch (Exception e) {
            if (e.getMessage().toLowerCase().contains("not found"))
                log.error("{} {} {}", doi, e.getClass(), e.getMessage());
            else
                log.error("Could not retrieve file metas for DOI: {}", doi, e);
            return;
        }
        for (DatasetVersion v : versions) {
            int fileCount = 0;
            for (FileMeta f : v.getFiles()) {
                saveActual(toActual(f, doi, v.getVersionNumber(), v.getVersionMinorNumber(), v.isFileAccessRequest()));
                ++fileCount;
            }
            log.info("Stored {} actual files for DOI {}, Version {}.{} State {}", fileCount, doi, v.getVersionNumber(), v.getVersionMinorNumber(), v.getVersionState());
        }
    }

    private ActualFile toActual(FileMeta fileMeta, String doi, int majorVersion, int minorVersion, boolean datasetHasAccessRequestEnabled) {
        DataFile f = fileMeta.getDataFile();
        String dl = fileMeta.getDirectoryLabel();
        String actual_path = (dl == null ? "" : dl + "/") + fileMeta.getLabel();
        ActualFile actualFile = new ActualFile(doi, actual_path, majorVersion, minorVersion, f.getChecksum().getValue(), f.getStorageIdentifier());
        actualFile.setAccessibleTo(fileMeta.getRestricted(), datasetHasAccessRequestEnabled);
        Embargo embargo = f.getEmbargo();
        if (embargo != null)
            actualFile.setEmbargo_date(embargo.getDateAvailable());
        return actualFile;
    }
}
