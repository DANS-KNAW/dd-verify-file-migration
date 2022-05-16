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

import nl.knaw.dans.migration.core.AccessCategory;
import nl.knaw.dans.migration.core.FedoraToBagCsv;
import nl.knaw.dans.migration.core.FileRights;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Index;
import javax.persistence.Table;
import java.io.File;
import java.util.Objects;

@Entity
@IdClass(ExpectedDatasetKey.class)
@Table(name = "expected_datasets",
       indexes = {
           @Index(name = "ed_doi_index", columnList = "doi")
       }
)
public class InputDataset {
    // https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#schema-generation

    // most lengths from easy-dtap/provisioning/roles/easy-fs-rdb/templates/create-easy-db-tables.sql
    // doi length as in dd-dtap/shared-code/dataverse/scripts/database/create/create_v*.sql

    public InputDataset (FedoraToBagCsv csv, File batch) {
        doi = csv.getDoi();
        easy_dataset_id = csv.getDatasetId();
        uuid_v1 = csv.getUuid1();
        uuid_v2 = csv.getUuid2();
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
    private String easy_dataset_id;

    @Column(name = "uuid_v1")
    private String uuid_v1;

    @Column(name = "uuid_v2")
    private String uuid_v2;

    @Column(name = "status")
    private String status;

    @Column(name = "comment")
    private String comment;

    @Column(name = "batch")
    private String batch;

    @Column(name = "source")
    private String source;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        InputDataset that = (InputDataset) o;
        return Objects.equals(doi, that.doi) && Objects.equals(easy_dataset_id, that.easy_dataset_id) && Objects.equals(uuid_v1, that.uuid_v1) && Objects.equals(
            uuid_v2, that.uuid_v2) && Objects.equals(status, that.status) && Objects.equals(comment, that.comment) && Objects.equals(batch, that.batch)
            && Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(doi, easy_dataset_id, uuid_v1, uuid_v2, status, comment, batch, source);
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }
}
