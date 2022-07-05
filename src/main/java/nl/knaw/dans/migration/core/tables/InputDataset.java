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

import nl.knaw.dans.migration.core.BagInfo;
import nl.knaw.dans.migration.core.FedoraToBagCsv;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Index;
import javax.persistence.Table;
import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@Entity
@IdClass(InputDatasetKey.class)
@Table(name = "input_datasets",
       indexes = {
           @Index(name = "id_doi_index", columnList = "doi"),
           @Index(name = "id_status_index", columnList = "status")
       }
)
public class InputDataset {
    // https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#schema-generation

    // most lengths from easy-dtap/provisioning/roles/easy-fs-rdb/templates/create-easy-db-tables.sql
    // doi length as in dd-dtap/shared-code/dataverse/scripts/database/create/create_v*.sql

    public InputDataset() {}

    public InputDataset(BagInfo bagInfo, String[] bagSeq, String batch, String source) {
        setDoi(bagInfo.getDoi());
        setEasyDatasetId("");
        setUuidV1(bagInfo.getBaseId());
        setUuidV2(Arrays.toString(bagSeq));
        setBatch(batch);
        setStatus("OK");
        setSource(source);
    }

    public InputDataset(BagInfo bagInfo, String status, String batch, String source) {
        setDoi(bagInfo.getDoi());
        setEasyDatasetId("");
        setUuidV1(bagInfo.getBaseId());
        setUuidV2("");
        setBatch(batch);
        setStatus(status);
        setSource(source);
    }

    public InputDataset(UUID uuid, String status, String batch, String source) {
        setDoi("");
        setEasyDatasetId("");
        setUuidV1(uuid.toString());
        setUuidV2("");
        setBatch(batch);
        setStatus(status);
        setSource(source);
    }

    public InputDataset (FedoraToBagCsv csv, File batch) {
        doi = csv.getDoi();
        easyDatasetId = csv.getDatasetId();
        uuidV1 = csv.getUuid1();
        uuidV2 = csv.getUuid2();
        comment = csv.getComment().replaceAll("\n.*","");
        this.batch = batch.toString();
        source = "fedora";
        status = (csv.getComment().contains("OK")) ? "OK"
            :(csv.getComment().contains("FAILED")) ? "FAILED"
            : "IGNORED";
    }

    @Id
    @Column(length = 255)
    private String doi;

    @Column(name = "easy_dataset_id")
    private String easyDatasetId;

    @Column(name = "uuid_v1")
    private String uuidV1;

    @Column(name = "uuid_v2")
    private String uuidV2;

    @Column(name = "status")
    private String status;

    @Column(name = "comment")
    private String comment;

    @Column(name = "batch")
    private String batch;

    @Column(name = "source")
    private String source;

    @Override
    public String toString() {
        return "InputDataset{" +
            "doi='" + doi + '\'' +
            ", easyDatasetId='" + easyDatasetId + '\'' +
            ", uuidV1='" + uuidV1 + '\'' +
            ", uuidV2='" + uuidV2 + '\'' +
            ", status='" + status + '\'' +
            ", comment='" + comment + '\'' +
            ", batch='" + batch + '\'' +
            ", source='" + source + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        InputDataset that = (InputDataset) o;
        return Objects.equals(doi, that.doi) && Objects.equals(easyDatasetId, that.easyDatasetId) && Objects.equals(uuidV1, that.uuidV1) && Objects.equals(
            uuidV2, that.uuidV2) && Objects.equals(status, that.status) && Objects.equals(comment, that.comment) && Objects.equals(batch, that.batch)
            && Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(doi, easyDatasetId, uuidV1, uuidV2, status, comment, batch, source);
    }

    public String getEasyDatasetId() {
        return easyDatasetId;
    }

    public void setEasyDatasetId(String easyDatasetId) {
        this.easyDatasetId = easyDatasetId;
    }

    public String getUuidV1() {
        return uuidV1;
    }

    public void setUuidV1(String uuidV1) {
        this.uuidV1 = uuidV1;
    }

    public String getUuidV2() {
        return uuidV2;
    }

    public void setUuidV2(String uuidV2) {
        this.uuidV2 = uuidV2;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }
}
