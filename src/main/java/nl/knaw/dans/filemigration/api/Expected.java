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
  private String easyFileId;
  @Column(name="fsRdbPath")
  private String fsRdbPath;
  @Column(name="expectedPath")
  private String expectedPath;
  @Column(name="addedDuringMigration")
  private boolean addedDuringMigration;
  @Column(name="removedThumbnail")
  private boolean removedThumbnail;
  @Column(name="removedOriginalDirectory")
  private boolean removedOriginalDirectory;
  @Column(name="removedDuplicateFile")
  private boolean removedDuplicateFile;
  @Column(name="transformedName")
  private boolean transformedName;

  public boolean isTransformedName() {
    return transformedName;
  }

  public void setTransformedName(boolean transformedName) {
    this.transformedName = transformedName;
  }

  public boolean isRemovedDuplicateFile() {
    return removedDuplicateFile;
  }

  public void setRemovedDuplicateFile(boolean removedDuplicateFile) {
    this.removedDuplicateFile = removedDuplicateFile;
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

  public String getSha1checksum() {
    return sha1checksum;
  }

  public void setSha1checksum(String sha1checksum) {
    this.sha1checksum = sha1checksum;
  }
}
