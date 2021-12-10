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

import javax.persistence.*;
import java.util.Objects;

@Entity
@IdClass(ExpectedFileKey.class)
@Table(name = "expected")
public class ExpectedFile {
  // https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#schema-generation

  public ExpectedFile() {}

  public ExpectedFile(String doi, String expected_path, int removed_duplicate_file_count, boolean removed_original_directory, String sha1_checksum, String easy_file_id, String fs_rdb_path, boolean added_during_migration, boolean removed_thumbnail, boolean transformed_name) {
    this.doi = doi;
    this.expected_path = expected_path;
    this.removed_duplicate_file_count = removed_duplicate_file_count;
    this.removed_original_directory = removed_original_directory;
    this.sha1_checksum = sha1_checksum;
    this.easy_file_id = easy_file_id;
    this.fs_rdb_path = fs_rdb_path;
    this.added_during_migration = added_during_migration;
    this.removed_thumbnail = removed_thumbnail;
    this.transformed_name = transformed_name;
  }

  // most lengths from easy-dtap/provisioning/roles/easy-fs-rdb/templates/create-easy-db-tables.sql
  // doi length as in dd-dtap/shared-code/dataverse/scripts/database/create/create_v*.sql

  @Id
  @Column(length = 255)
  private String doi;

  @Id
  @Column(length = 1024) // TODO basic_file_meta has only 1000
  private String expected_path;

  @Id
  @Column()
  private int removed_duplicate_file_count;

  @Column()
  private boolean removed_original_directory;

  @Column(length = 40)
  private String sha1_checksum = "";

  @Column(length = 64)
  private String easy_file_id = "";

  @Column(length = 1024)
  private String fs_rdb_path = "";

  @Column()
  private boolean added_during_migration;

  @Column()
  private boolean removed_thumbnail;

  @Column()
  private boolean transformed_name;

  @Override
  public String toString() {
    return "ExpectedFile{" + "doi='" + doi + '\'' + ", expected_path='" + expected_path + '\'' + ", removed_duplicate_file_count=" + removed_duplicate_file_count + ", removed_original_directory=" + removed_original_directory + ", sha1_checksum='" + sha1_checksum + '\'' + ", easy_file_id='" + easy_file_id + '\'' + ", fs_rdb_path='" + fs_rdb_path + '\'' + ", added_during_migration=" + added_during_migration + ", removed_thumbnail=" + removed_thumbnail + ", transformed_name=" + transformed_name + '}';
  }

  public boolean isTransformed_name() {
    return transformed_name;
  }

  public void setTransformed_name(boolean transformed_name) {
    this.transformed_name = transformed_name;
  }

  public int getRemoved_duplicate_file_count() {
    return removed_duplicate_file_count;
  }

  public void setRemoved_duplicate_file_count(int removed_duplicate_file_count) {
    this.removed_duplicate_file_count = removed_duplicate_file_count;
  }

  public void incRemoved_duplicate_file_count() {
    this.removed_duplicate_file_count += 1;
  }

  public boolean isRemoved_original_directory() {
    return removed_original_directory;
  }

  public void setRemoved_original_directory(boolean removed_original_directory) {
    this.removed_original_directory = removed_original_directory;
  }

  public boolean isRemoved_thumbnail() {
    return removed_thumbnail;
  }

  public void setRemoved_thumbnail(boolean removed_thumbnail) {
    this.removed_thumbnail = removed_thumbnail;
  }

  public boolean isAdded_during_migration() {
    return added_during_migration;
  }

  public void setAdded_during_migration(boolean added_during_migration) {
    this.added_during_migration = added_during_migration;
  }

  public String getExpected_path() {
    return expected_path;
  }

  public void setExpected_path(String expected_path) {
    this.expected_path = expected_path;
  }

  public String getFs_rdb_path() {
    return fs_rdb_path;
  }

  public void setFs_rdb_path(String fs_rdb_path) {
    this.fs_rdb_path = fs_rdb_path;
  }

  public String getEasy_file_id() {
    return easy_file_id;
  }

  public void setEasy_file_id(String easy_file_id) {
    this.easy_file_id = easy_file_id;
  }

  public String getSha1_checksum() {
    return sha1_checksum;
  }

  public void setSha1_checksum(String sha1_checksum) {
    this.sha1_checksum = sha1_checksum;
  }

  public String getDoi() {
    return doi;
  }

  public void setDoi(String doi) {
    this.doi = doi;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    ExpectedFile that = (ExpectedFile) o;
    return removed_duplicate_file_count == that.removed_duplicate_file_count && removed_original_directory == that.removed_original_directory && added_during_migration == that.added_during_migration && removed_thumbnail == that.removed_thumbnail && transformed_name == that.transformed_name && Objects.equals(doi, that.doi) && Objects.equals(expected_path, that.expected_path) && Objects.equals(sha1_checksum, that.sha1_checksum) && Objects.equals(easy_file_id, that.easy_file_id) && Objects.equals(fs_rdb_path, that.fs_rdb_path);
  }

  @Override
  public int hashCode() {
    return Objects.hash(doi, expected_path, removed_duplicate_file_count, removed_original_directory, sha1_checksum, easy_file_id, fs_rdb_path, added_during_migration, removed_thumbnail, transformed_name);
  }
}
