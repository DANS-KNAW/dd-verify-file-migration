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
package nl.knaw.dans.migration.core.tables;

import nl.knaw.dans.migration.core.FileRights;
import nl.knaw.dans.migration.core.ManifestCsv;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@IdClass(ExpectedFileKey.class)
@Table(name = "expected_files",
       indexes = {
           @Index(name = "ef_accessible_index", columnList = "accessible_to")
       }
)
public class ExpectedFile {
    // https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#schema-generation

    public ExpectedFile() {
    }

    public ExpectedFile(String doi, EasyFile easyFile, boolean removeOriginal) {
        final String path = removeOriginal
                ? easyFile.getPath().replace("original/", "")
                : easyFile.getPath();
        final String dvPath = dvPath(path);

        setDoi(doi);
        setSha1Checksum(easyFile.getSha1Checksum());
        setEasyFileId(easyFile.getPid());
        setFsRdbPath(easyFile.getPath());
        setExpectedPath(dvPath);
        setAccessibleTo(easyFile.getAccessibleTo());
        setVisibleTo(easyFile.getVisibleTo());
        setAddedDuringMigration(false);
        setRemovedThumbnail(path.toLowerCase().matches(".*thumbnails/.*_small.(png|jpg|tiff)"));
        setRemovedOriginalDirectory(removeOriginal);
        setTransformedName(!path.equals(dvPath));
    }

    public ExpectedFile(String doi, ManifestCsv manifestCsv, FileRights fileRights) {
        final String path = manifestCsv.getPath();
        final String dvPath = dvPath(path);

        setDoi(doi);
        setSha1Checksum(manifestCsv.getSha1());
        setEasyFileId("");
        setFsRdbPath(manifestCsv.getPath());
        setExpectedPath(dvPath);
        setAccessibleTo(fileRights.getAccessibleTo());
        setVisibleTo(fileRights.getVisibleTo());
        setAddedDuringMigration(false);
        setRemovedThumbnail(path.toLowerCase().matches(".*thumbnails/.*_small.(png|jpg|tiff)"));
        setRemovedOriginalDirectory(false);
        setTransformedName(!path.equals(dvPath));
    }

    private String dvPath(String path) {
        final String file = replaceForbidden(path.replaceAll(".*/", ""), forbiddenInFileName);
        final String folder = replaceForbidden(path.replaceAll("[^/]*$", ""), forbiddenInFolders);
        final String dvPath = folder + file;
        return dvPath;
    }

    private static final String forbidden = ":*?\"<>|;#";
    private static final char[] forbiddenInFileName = ":*?\"<>|;#".toCharArray();
    private static final char[] forbiddenInFolders = (forbidden + "'(),[]&+'").toCharArray();

    private static String replaceForbidden(String s, char[] forbidden) {
        for (char c : forbidden)
            s = s.replace(c, '_');
        return s;
    }

    public ExpectedFile(String doi, String expectedPath, boolean removedOriginalDirectory, String sha1Checksum, String easyFileId, String fsRdbPath,
        boolean addedDuringMigration, boolean removedThumbnail, boolean transformedName, String accessibleTo, String visibleTo) {
        this.doi = doi;
        this.expectedPath = expectedPath;
        this.removedOriginalDirectory = removedOriginalDirectory;
        this.sha1Checksum = sha1Checksum;
        this.easyFileId = easyFileId;
        this.fsRdbPath = fsRdbPath;
        this.addedDuringMigration = addedDuringMigration;
        this.removedThumbnail = removedThumbnail;
        this.transformedName = transformedName;
        this.accessibleTo = accessibleTo;
        this.visibleTo = visibleTo;
    }

    // most lengths from easy-dtap/provisioning/roles/easy-fs-rdb/templates/create-easy-db-tables.sql
    // doi length as in dd-dtap/shared-code/dataverse/scripts/database/create/create_v*.sql

    @Id
    @Column(length = 255)
    private String doi;

    @Id
    @Column(name="expected_path", length = 1024) // TODO basic_file_meta has only 1000
    private String expectedPath;

    @Column(name="removed_original_directory")
    private boolean removedOriginalDirectory;

    @Column(name="sha1_checksum", length = 40)
    private String sha1Checksum = "";

    /**
     * abused for the bag sequence nr when loading from the bag-store (alias vault)
     */
    @Column(name="easy_file_id", length = 64)
    private String easyFileId = "";

    @Column(name="fs_rdb_path", length = 1024)
    private String fsRdbPath = "";

    @Column(name="added_during_migration")
    private boolean addedDuringMigration;

    @Column(name="removed_thumbnail")
    private boolean removedThumbnail;

    @Column(name="transformed_name")
    private boolean transformedName;

    @Column(name="accessible_to")
    private String accessibleTo;

    @Column(name="visible_to")
    private String visibleTo;

    @Nullable
    @Column(name="embargo_date")
    private String embargoDate;

    @Override
    public String toString() {
        return "ExpectedFile{" +
                "doi='" + doi + '\'' +
                ", expectedPath='" + expectedPath + '\'' +
                ", removedOriginalDirectory=" + removedOriginalDirectory +
                ", sha1Checksum='" + sha1Checksum + '\'' +
                ", easyFileId='" + easyFileId + '\'' +
                ", fsRdbPath='" + fsRdbPath + '\'' +
                ", addedDuringMigration=" + addedDuringMigration +
                ", removedThumbnail=" + removedThumbnail +
                ", transformedName=" + transformedName +
                ", accessibleTo='" + accessibleTo + '\'' +
                ", visibleTo='" + visibleTo + '\'' +
                ", embargoDate='" + embargoDate + '\'' +
                '}';
    }

    public boolean isTransformedName() {
        return transformedName;
    }

    public void setTransformedName(boolean transformedName) {
        this.transformedName = transformedName;
    }

    public boolean isRemovedOriginalDirectory() {
        return removedOriginalDirectory;
    }

    public void setRemovedOriginalDirectory(boolean removedOriginalDirectory) {
        this.removedOriginalDirectory = removedOriginalDirectory;
    }

    public boolean isRemovedThumbnail() {
        return removedThumbnail;
    }

    public void setRemovedThumbnail(boolean removedThumbnail) {
        this.removedThumbnail = removedThumbnail;
    }

    public boolean isAddedDuringMigration() {
        return addedDuringMigration;
    }

    public void setAddedDuringMigration(boolean addedDuringMigration) {
        this.addedDuringMigration = addedDuringMigration;
    }

    public String getExpectedPath() {
        return expectedPath;
    }

    public void setExpectedPath(String expectedPath) {
        this.expectedPath = expectedPath;
    }

    public String getFsRdbPath() {
        return fsRdbPath;
    }

    public void setFsRdbPath(String fsRdbPath) {
        this.fsRdbPath = fsRdbPath;
    }

    public String getEasyFileId() {
        return easyFileId;
    }

    public void setEasyFileId(String easyFileId) {
        this.easyFileId = easyFileId;
    }

    public String getSha1Checksum() {
        return sha1Checksum;
    }

    public void setSha1Checksum(String sha1Checksum) {
        this.sha1Checksum = sha1Checksum;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    @Nullable
    public String getEmbargoDate() {
        return embargoDate;
    }

    public void setDefaultRights(@NonNull FileRights fileRights) {
        accessibleTo = fileRights.getAccessibleTo();
        visibleTo = fileRights.getVisibleTo();
        // the logic for a date in the future is in the hands of fileRights
        embargoDate = fileRights.getEmbargoDate();
    }

    public String getAccessibleTo() {
        return accessibleTo;
    }

    public void setAccessibleTo(String accessibleTo) {
        // do not override the effect of setDefaultRights with nothing
        if (!StringUtils.isEmpty(accessibleTo))
            this.accessibleTo = accessibleTo;
    }

    public String getVisibleTo() {
        return visibleTo;
    }

    public void setVisibleTo(String visibleTo) {
        // do not override the effect of setDefaultRights with nothing
        if (!StringUtils.isEmpty(visibleTo))
            this.visibleTo = visibleTo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpectedFile that = (ExpectedFile) o;
        return removedOriginalDirectory == that.removedOriginalDirectory && addedDuringMigration == that.addedDuringMigration && removedThumbnail == that.removedThumbnail && transformedName == that.transformedName && Objects.equals(doi, that.doi) && Objects.equals(expectedPath, that.expectedPath) && Objects.equals(sha1Checksum, that.sha1Checksum) && Objects.equals(easyFileId, that.easyFileId) && Objects.equals(fsRdbPath, that.fsRdbPath) && Objects.equals(accessibleTo, that.accessibleTo) && Objects.equals(visibleTo, that.visibleTo) && Objects.equals(embargoDate, that.embargoDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(doi, expectedPath, removedOriginalDirectory, sha1Checksum, easyFileId, fsRdbPath, addedDuringMigration, removedThumbnail, transformedName, accessibleTo, visibleTo, embargoDate);
    }
}
