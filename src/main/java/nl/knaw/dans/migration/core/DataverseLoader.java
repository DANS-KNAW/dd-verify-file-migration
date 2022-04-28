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
import com.github.benmanes.caffeine.cache.CacheLoader;
import io.dropwizard.hibernate.UnitOfWork;
import nl.knaw.dans.lib.dataverse.DataverseClient;
import nl.knaw.dans.lib.dataverse.DataverseResponse;
import nl.knaw.dans.lib.dataverse.model.RoleAssignmentReadOnly;
import nl.knaw.dans.lib.dataverse.model.dataset.DatasetLatestVersion;
import nl.knaw.dans.lib.dataverse.model.dataset.DatasetVersion;
import nl.knaw.dans.lib.dataverse.model.file.DataFile;
import nl.knaw.dans.lib.dataverse.model.file.Embargo;
import nl.knaw.dans.lib.dataverse.model.file.FileMeta;
import nl.knaw.dans.lib.dataverse.model.user.AuthenticatedUser;
import nl.knaw.dans.migration.core.tables.ActualDataset;
import nl.knaw.dans.migration.core.tables.ActualFile;
import nl.knaw.dans.migration.db.ActualDatasetDAO;
import nl.knaw.dans.migration.db.ActualFileDAO;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.hsqldb.lib.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

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
    public void deleteCsvDOIs(CSVParser csvRecords, Mode mode) {
        for (CSVRecord r : csvRecords) {
            FedoraToBagCsv fedoraToBagCsv = new FedoraToBagCsv(r);
            if (fedoraToBagCsv.getComment().contains("OK")) {
                deleteByDoi(fedoraToBagCsv.getDoi(), mode);
            }
        }
    }

    @UnitOfWork("hibernate")
    public void deleteSingleDoi(String doi, Mode mode) {
        deleteByDoi(doi.replace("doi:",""), mode);
    }

    @UnitOfWork("hibernate")
    public void deleteAll(Mode mode) {
        if (mode.doFiles())
            actualFileDAO.deleteAll();
        if (mode.doDatasets())
            actualDatasetDAO.deleteAll();
    }

    public void deleteByDoi(String doi, Mode mode) {
        if (mode.doFiles())
            actualFileDAO.deleteByDoi(doi);
        if (mode.doDatasets())
            actualDatasetDAO.deleteByDoi(doi);
    }

    @UnitOfWork("hibernate")
    public void loadFromDataset(String doi, Mode mode) {
        if (StringUtil.isEmpty(doi))
            return; // workaround
        log.info("Reading {} from dataverse", doi);

        CacheLoader<String, DataverseResponse<AuthenticatedUser>> userLoader = id -> client.admin().listSingleUser(id);
        CacheLoader<String, DataverseResponse<List<DatasetVersion>>> versionsLoader = id -> client.dataset(id).getAllVersions();
        CacheLoader<String, DataverseResponse<List<RoleAssignmentReadOnly>>> rolesLoader = id -> client.dataset(id).listRoleAssignments();
        CacheLoader<String, DataverseResponse<DatasetLatestVersion>> lastestVersionLoader = id -> client.dataset(id).viewLatestVersion();

        String shortDoi = doi.replace("doi:", "");
        load(doi, versionsLoader, DatasetVersion.class, doi).ifPresent(versions ->
            versions.forEach(v -> {
                if (mode.doDatasets()) {
                    ActualDataset actualDataset = new ActualDataset();
                    actualDataset.setMajorVersionNr(v.getVersionNumber());
                    actualDataset.setMinorVersionNr(v.getVersionMinorNumber());
                    actualDataset.setDeaccessioned("DEACCESSIONED".equals(v.getVersionState()));
                    actualDataset.setLicenseName(v.getLicense().getName());
                    actualDataset.setLicenseUri(v.getLicense().getUri().toString());
                    actualDataset.setDoi(shortDoi);
                    load(doi, lastestVersionLoader, DatasetLatestVersion.class, doi).ifPresent(lastestVersion -> {
                        actualDataset.setCitationYear(lastestVersion.getPublicationDate().substring(0, 4));
                        actualDataset.setFileAccessRequest(lastestVersion.getLatestVersion().isFileAccessRequest());
                    });
                    load(doi, rolesLoader, RoleAssignmentReadOnly.class, doi).ifPresent(roles -> {
                        String depositor = roles.stream()
                            .filter(ra -> "contributorplus".equals(ra.get_roleAlias()))
                            .findFirst()
                            .map(RoleAssignmentReadOnly::getAssignee)
                            .orElse("contributorplus.not.found")
                            .replace("@", "");
                        log.trace("depositor: " + depositor);
                        actualDataset.setDepositor(depositor); // falling back to ID if email is not found
                        load(depositor, userLoader, AuthenticatedUser.class, doi).ifPresent(user ->
                            actualDataset.setDepositor(user.getEmail())
                        );
                    });
                    actualDatasetDAO.create(actualDataset);
                }
                if (mode.doFiles())
                    loadFiles(shortDoi, v);
            })
        );
    }

    private <T> Optional<T> load(String id, CacheLoader<String, DataverseResponse<T>> loader, Class<?> clazz, String doi) {
        // actually T might be a List of clazz
        try {
            DataverseResponse<T> response = loader.load(id);
            if (null != response)
                return Optional.ofNullable(response.getData());
        }
        catch (JsonParseException | UnrecognizedPropertyException  e) {
            // e.g: Could not parse AuthenticatedUser(user001) ... JsonParseException Unexpected character ('<' ...)
            // when not running on localhost of dataverse.
            // response.getEnvelopeAsString() is the dataverse Home page, in this specific case.
            log.error("Could not parse {}({}) while processing {}: {} {}", clazz.getSimpleName() , id, doi, e.getClass(), e.getMessage());
        }
        catch (Exception e) {
            if (e.getMessage().toLowerCase().contains("not found"))
                log.error("Could not find {}({}) while processing {}: {} {}", clazz.getSimpleName() , id, doi, e.getClass(), e.getMessage());
            else
                log.error("Could not retrieve {}({}) while processing {}: {}", clazz.getSimpleName(), id, doi, e);
        }
        return Optional.empty();
    }

    private void loadFiles(String doi, DatasetVersion v) {
        for (FileMeta f : v.getFiles()) {
            actualFileDAO.create(toActual(f, doi, v));
        }
        log.info("Stored {} actual files for DOI {}, Version {}.{} State {}", v.getFiles().size(), doi, v.getVersionNumber(), v.getVersionMinorNumber(), v.getVersionState());
    }

    private ActualFile toActual(FileMeta fileMeta, String doi, DatasetVersion v) {
        DataFile f = fileMeta.getDataFile();
        String dl = fileMeta.getDirectoryLabel();
        String actualPath = (dl == null ? "" : dl + "/") + fileMeta.getLabel();
        ActualFile actualFile = new ActualFile();
        actualFile.setDoi(doi);
        actualFile.setActualPath(actualPath);
        actualFile.setMajorVersionNr(v.getVersionNumber());
        actualFile.setMinorVersionNr(v.getVersionMinorNumber());
        actualFile.setSha1Checksum(f.getChecksum().getValue());
        actualFile.setStorageId(v.getStorageIdentifier());
        actualFile.setAccessibleTo(fileMeta.getRestricted(), v.isFileAccessRequest());
        Embargo embargo = f.getEmbargo();
        if (embargo != null)
            actualFile.setEmbargoDate(embargo.getDateAvailable());
        return actualFile;
    }
}
