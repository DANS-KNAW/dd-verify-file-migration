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
@Table(name = "easy_files")
@NamedQueries({ @NamedQuery(name = "EasyFile.findByDatasetId",
                            query = "SELECT * FROM EasyFile ef WHERE ef.dataset_sid = :datasedSid ORDER BY path"),
})
public class EasyFile {
  public static final String FIND_BY_DATASET_ID ="EasyFile.findByDatasetId";
  public static final String DATASET_ID ="datasetSid";

  @Id
  @Column(name = "pid")
  private String pid;
  @Column(name = "parent_sid")
  private String parentSid;
  @Column(name = "dataset_sid")
  private String datasetSid;
  @Column(name = "path")
  private String path;
  @Column(name = "filename")
  private String filename;
  @Column(name = "size")
  private long size;
  @Column(name = "mimetype")
  private String mimetype;
  @Column(name = "creator_role")
  private String creatorRole;
  @Column(name = "visible_to")
  private String visibleTo;
  @Column(name = "accessible_to")
  private String accessibleTo;
  @Column(name = "sha1checksum")
  private String sha1checksum;

  public String getSha1checksum() {
    return sha1checksum;
  }

  public void setSha1checksum(String sha1checksum) {
    this.sha1checksum = sha1checksum;
  }

  public String getAccessibleTo() {
    return accessibleTo;
  }

  public void setAccessibleTo(String accessibleTo) {
    this.accessibleTo = accessibleTo;
  }

  public String getVisibleTo() {
    return visibleTo;
  }

  public void setVisibleTo(String visibleTo) {
    this.visibleTo = visibleTo;
  }

  public String getCreatorRole() {
    return creatorRole;
  }

  public void setCreatorRole(String creatorRole) {
    this.creatorRole = creatorRole;
  }

  public String getMimetype() {
    return mimetype;
  }

  public void setMimetype(String mimetype) {
    this.mimetype = mimetype;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getDatasetSid() {
    return datasetSid;
  }

  public void setDatasetSid(String datasetSid) {
    this.datasetSid = datasetSid;
  }

  public String getParentSid() {
    return parentSid;
  }

  public void setParentSid(String parentSid) {
    this.parentSid = parentSid;
  }

  public String getPid() {
    return pid;
  }

  public void setPid(String pid) {
    this.pid = pid;
  }
}
