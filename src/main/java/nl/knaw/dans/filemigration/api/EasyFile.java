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
                            query = "SELECT ef FROM SELECT ef FROM nl.knaw.dans.filemigration.api.EasyFile ef WHERE ef.datasetSid = :datased_sid ORDER BY ef.path",
                            hints = {
                                @QueryHint(
                                    name = "org.hibernate.readOnly",
                                    value = "true"
                                )
                            }),
})
public class EasyFile {
  public static final String FIND_BY_DATASET_ID ="EasyFile.findByDatasetId";
  public static final String DATASET_ID ="dataset_sid";

  @Id
  @Column(name = "pid", nullable = false)
  private String pid;
  @Column(name = "parent_sid", nullable = false)
  private String parent_sid;
  @Column(name = "dataset_sid", nullable = false)
  private String dataset_sid;
  @Column(name = "path")
  private String path;
  @Column(name = "filename", nullable = false)
  private String filename;
  @Column(name = "size", nullable = false)
  private long size;
  @Column(name = "mimetype", nullable = false)
  private String mimetype;
  @Column(name = "creator_role", nullable = false)
  private String creator_role;
  @Column(name = "visible_to", nullable = false)
  private String visible_to;
  @Column(name = "accessible_to", nullable = false)
  private String accessible_to;
  @Column(name = "sha1checksum")
  private String sha1checksum;

  public String getSha1checksum() {
    return sha1checksum;
  }

  public void setSha1checksum(String sha1checksum) {
    this.sha1checksum = sha1checksum;
  }

  public String getAccessible_to() {
    return accessible_to;
  }

  public void setAccessible_to(String accessible_to) {
    this.accessible_to = accessible_to;
  }

  public String getVisible_to() {
    return visible_to;
  }

  public void setVisible_to(String visible_to) {
    this.visible_to = visible_to;
  }

  public String getCreator_role() {
    return creator_role;
  }

  public void setCreator_role(String creator_role) {
    this.creator_role = creator_role;
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

  public String getDataset_sid() {
    return dataset_sid;
  }

  public void setDataset_sid(String dataset_sid) {
    this.dataset_sid = dataset_sid;
  }

  public String getParent_sid() {
    return parent_sid;
  }

  public void setParent_sid(String parent_sid) {
    this.parent_sid = parent_sid;
  }

  public String getPid() {
    return pid;
  }

  public void setPid(String pid) {
    this.pid = pid;
  }
}
