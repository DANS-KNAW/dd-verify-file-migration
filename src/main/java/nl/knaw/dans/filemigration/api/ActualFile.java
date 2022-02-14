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
package nl.knaw.dans.filemigration.api;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@IdClass(ActualFileKey.class)
@Table(name = "actual")
public class ActualFile {
  // https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#schema-generation

  public ActualFile() {}

  public ActualFile(String doi, String actual_path, int major_version_nr, int minor_version_nr, String sha1_checksum, String storage_id) {
    this.doi = doi;
    this.actual_path = actual_path;
    this.major_version_nr = major_version_nr;
    this.minor_version_nr = minor_version_nr;
    this.sha1_checksum = sha1_checksum;
    this.storage_id = storage_id;
  }
  // most lengths from easy-dtap/provisioning/roles/easy-fs-rdb/templates/create-easy-db-tables.sql
  // doi length as in dd-dtap/shared-code/dataverse/scripts/database/create/create_v*.sql

  @Id
  @Column(length = 255)
  private String doi;

  @Id
  @Column(length = 1024) // TODO basic_file_meta has only 1000
  private String actual_path;

  @Id
  @Column()
  private int major_version_nr;

  @Id
  @Column()
  private int minor_version_nr;

  @Column(length = 40)
  private String sha1_checksum = "";

  @Column(length = 60)
  private String storage_id = "";

  @Nullable
  @Column()
  private String embargo_date;

  public int getMajor_version_nr() {
    return major_version_nr;
  }

  public void setMajor_version_nr(int major_version_nr) {
    this.major_version_nr = major_version_nr;
  }

  public void setMinor_version_nr(int minor_version_nr) {
    this.minor_version_nr = minor_version_nr;
  }

  public int getMinor_version_nr() {
    return minor_version_nr;
  }

  public String getActual_path() {
    return actual_path;
  }

  public void setActual_path(String actual_path) {
    this.actual_path = actual_path;
  }

  public String getStorage_id() {
    return storage_id;
  }

  public void setStorage_id(String storage_id) {
    this.storage_id = storage_id;
  }

  public String getSha1_checksum() {
    return sha1_checksum;
  }

  public void setSha1_checksum(String sha1_checksum) {
    this.sha1_checksum = sha1_checksum;
  }

  @Nullable
  public String getEmbargo_date() {
    return embargo_date;
  }

  public void setEmbargo_date(@Nullable String embargo_date) {
    this.embargo_date = embargo_date;
  }

  public String getDoi() {
    return doi;
  }

  public void setDoi(String doi) {
    this.doi = doi;
  }

  @Override
  public String toString() {
    return "ActualFile{" +
            "doi='" + doi + '\'' +
            ", actual_path='" + actual_path + '\'' +
            ", major_version_nr=" + major_version_nr +
            ", minor_version_nr=" + minor_version_nr +
            ", sha1_checksum='" + sha1_checksum + '\'' +
            ", storage_id='" + storage_id + '\'' +
            ", embargo_date='" + embargo_date + '\'' +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ActualFile that = (ActualFile) o;
    return major_version_nr == that.major_version_nr && minor_version_nr == that.minor_version_nr && Objects.equals(doi, that.doi) && Objects.equals(actual_path, that.actual_path) && Objects.equals(sha1_checksum, that.sha1_checksum) && Objects.equals(storage_id, that.storage_id) && Objects.equals(embargo_date, that.embargo_date);
  }

  @Override
  public int hashCode() {
    return Objects.hash(doi, actual_path, major_version_nr, minor_version_nr, sha1_checksum, storage_id, embargo_date);
  }
}
