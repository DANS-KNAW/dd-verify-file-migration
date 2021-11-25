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

@Entity
@Table(name = "expected")
public class Expected {

  @Id
  @Column(name="doi")
  private String doi;
  @Column(name="sha1checksum")
  private String sha1checksum;
  @Column(name="easy_file_id")
  private String easy_file_id;
  @Column(name="fs_rdb_path")
  private String fs_rdb_path;
  @Column(name="expected_path")
  private String expected_path;
  @Column(name="added_during_migration")
  private boolean added_during_migration;
  @Column(name="removed_thumbnail")
  private boolean removed_thumbnail;
  @Column(name="removed_original_directory")
  private boolean removed_original_directory;
  @Column(name="removed_duplicate_file")
  private boolean removed_duplicate_file;
  @Column(name="transformed_name")
  private boolean transformed_name;

  public String toString() {
    // TODO improve?
    return  doi + ",  " + sha1checksum + ",  " + easy_file_id + ",  " + fs_rdb_path + ",  " + expected_path + ",  " + added_during_migration + ",  " + removed_thumbnail + ",  " + removed_original_directory + ",  " + removed_duplicate_file + ",  " + transformed_name;
  }

  public boolean isTransformed_name() {
    return transformed_name;
  }

  public void setTransformed_name(boolean transformed_name) {
    this.transformed_name = transformed_name;
  }

  public boolean isRemoved_duplicate_file() {
    return removed_duplicate_file;
  }

  public void setRemoved_duplicate_file(boolean removed_duplicate_file) {
    this.removed_duplicate_file = removed_duplicate_file;
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

  public String getSha1checksum() {
    return sha1checksum;
  }

  public void setSha1checksum(String sha1checksum) {
    this.sha1checksum = sha1checksum;
  }

  public String getDoi() {
    return doi;
  }

  public void setDoi(String doi) {
    this.doi = doi;
  }
}
