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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import io.dropwizard.hibernate.UnitOfWork;
import nl.knaw.dans.lib.dataverse.DataverseClient;
import nl.knaw.dans.lib.dataverse.model.RoleAssignmentReadOnly;
import nl.knaw.dans.lib.dataverse.model.dataset.DatasetVersion;
import nl.knaw.dans.lib.dataverse.model.dataset.PrimitiveSingleValueField;
import nl.knaw.dans.lib.dataverse.model.file.DataFile;
import nl.knaw.dans.lib.dataverse.model.file.Embargo;
import nl.knaw.dans.lib.dataverse.model.file.FileMeta;
import nl.knaw.dans.migration.core.tables.ActualDataset;
import nl.knaw.dans.migration.core.tables.ActualFile;
import nl.knaw.dans.migration.db.ActualDatasetDAO;
import nl.knaw.dans.migration.db.ActualFileDAO;
import org.hsqldb.lib.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DataverseLoader {
    private static final Logger log = LoggerFactory.getLogger(DataverseLoader.class);

    private final ActualFileDAO actualFileDAO;
    private final DataverseClient client;
    private final ActualDatasetDAO actualDatasetDAO;

    public DataverseLoader(DataverseClient client, ActualFileDAO actualFileDAO, ActualDatasetDAO actualDatasetDAO) {
        this.actualFileDAO = actualFileDAO;
        this.client = client;
        this.actualDatasetDAO = actualDatasetDAO;
    }

    @UnitOfWork("hibernate")
    public void saveActualFile(ActualFile actual) {
        log.debug(actual.toString());
        actualFileDAO.create(actual);
    }

    @UnitOfWork("hibernate")
    public void saveActualDataset(ActualDataset actual) {
        log.debug(actual.toString());
        actualDatasetDAO.create(actual);
    }

    public void loadFromDataset(String doi) {
        if (StringUtil.isEmpty(doi))
            return; // workaround
        log.info("Reading {} from dataverse", doi);
        List<DatasetVersion> versions = new ArrayList<>();
        String depositor = "";
        try {
            versions = client.dataset(doi).getAllVersions().getData();
            depositor = client.dataset(doi).listRoleAssignments().getData().stream()
                .filter(ra -> "contributorplus".equals(ra.get_roleAlias()))
                .findFirst()
                .map(RoleAssignmentReadOnly::getAssignee)
                .orElse("not.found@dans.knaw.nl")
                .replace("@", "");
            depositor = client.admin().listSingleUser(depositor).getData().getEmail();
        }
        catch (JsonParseException e) {
            // a developer may encounter: "Endpoint available from localhost only" and/or receive an HTML page
            if (!"".equals(depositor))
                log.error("Could not access email {} {}", doi, depositor);
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
        String shortDoi = doi.replace("doi:", "");
        DatasetVersion lastVersion = versions.get(versions.size() - 1);
        for (DatasetVersion v : versions) {
            int fileCount = 0;
            for (FileMeta f : v.getFiles()) {
                saveActualFile(toActual(f, shortDoi, v.getVersionNumber(), v.getVersionMinorNumber(), v.isFileAccessRequest()));
                ++fileCount;
            }
            log.info("Stored {} actual files for DOI {}, Version {}.{} State {}", fileCount, doi, v.getVersionNumber(), v.getVersionMinorNumber(), v.getVersionState());
            ActualDataset actualDataset = new ActualDataset();
            actualDataset.setMajorVersionNr(v.getVersionNumber());
            actualDataset.setMinorVersionNr(v.getVersionMinorNumber());
            actualDataset.setDeaccessioned("DEACCESSIONED".equals(v.getVersionState()));
            actualDataset.setLicenseName(v.getLicense().getName());
            actualDataset.setLicenseUri(v.getLicense().getUri().toString());
            actualDataset.setDoi(shortDoi);
            actualDataset.setDepositor(depositor);
            actualDataset.setFileAccessRequest(lastVersion.isFileAccessRequest());
            v.getMetadataBlocks()
                .get("citation").getFields().stream()
                .filter(field -> "dateOfDeposit".equals(field.getTypeName()))
                .map(field -> ((PrimitiveSingleValueField)field).getValue())
                .forEach(date -> actualDataset.setCitationYear(date.substring(0,4)));
            saveActualDataset(actualDataset);
        }
    }

    private ActualFile toActual(FileMeta fileMeta, String doi, int majorVersion, int minorVersion, boolean datasetHasAccessRequestEnabled) {
        DataFile f = fileMeta.getDataFile();
        String dl = fileMeta.getDirectoryLabel();
        String actualPath = (dl == null ? "" : dl + "/") + fileMeta.getLabel();
        ActualFile actualFile = new ActualFile(doi, actualPath, majorVersion, minorVersion, f.getChecksum().getValue(), f.getStorageIdentifier());
        actualFile.setAccessibleTo(fileMeta.getRestricted(), datasetHasAccessRequestEnabled);
        Embargo embargo = f.getEmbargo();
        if (embargo != null)
            actualFile.setEmbargoDate(embargo.getDateAvailable());
        return actualFile;
    }
}
