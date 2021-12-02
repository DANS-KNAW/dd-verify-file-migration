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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.Table;
import java.util.Objects;

// order by in decreasing order, thus earlier versions become duplicates TODO how to by numeric part?
@NamedQueries({ @NamedQuery(name = "EasyFile.findByDatasetId",
                            query = "SELECT ef FROM EasyFile ef WHERE ef.dataset_sid = :dataset_sid ORDER BY ef.pid desc",
                            hints = {
                                @QueryHint(
                                    name = "org.hibernate.readOnly",
                                    value = "true"
                                )
                            }),
})

@Entity
@Table(name = "easy_files")
public class EasyFile {
  public static final String FIND_BY_DATASET_ID ="EasyFile.findByDatasetId";
  public static final String DATASET_ID ="dataset_sid";

  public EasyFile(){}

  public EasyFile(String pid, String parent_sid, String dataset_sid, String path, String filename, long size, String mimetype, String creator_role, String visible_to, String accessible_to, String sha1checksum) {
    this.pid = pid;
    this.parent_sid = parent_sid;
    this.dataset_sid = dataset_sid;
    this.path = path;
    this.filename = filename;
    this.size = size;
    this.mimetype = mimetype;
    this.creator_role = creator_role;
    this.visible_to = visible_to;
    this.accessible_to = accessible_to;
    this.sha1checksum = sha1checksum;
  }

  @Id
  @Column(nullable = false)
  private String pid = "";

  @Column(nullable = false)
  private String parent_sid = "";

  @Column(nullable = false)
  private String dataset_sid = "";

  @Column()
  private String path = "";

  @Column(nullable = false)
  private String filename = "";

  @Column(nullable = false)
  private long size;

  @Column(nullable = false)
  private String mimetype = "";

  @Column(nullable = false)
  private String creator_role = "";

  @Column(nullable = false)
  private String visible_to = "";

  @Column(nullable = false)
  private String accessible_to = "";

  @Column()
  private String sha1checksum = "";

  @Override
  public String toString() {
    return "EasyFile{" + "pid='" + pid + '\'' + ", parent_sid='" + parent_sid + '\'' + ", dataset_sid='" + dataset_sid + '\'' + ", path='" + path + '\'' + ", filename='" + filename + '\'' + ", size=" + size + ", mimetype='" + mimetype + '\'' + ", creator_role='" + creator_role + '\'' + ", visible_to='" + visible_to + '\'' + ", accessible_to='" + accessible_to + '\'' + ", sha1checksum='" + sha1checksum + '\'' + '}';
  }

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

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    EasyFile easyFile = (EasyFile) o;
    return size == easyFile.size && Objects.equals(pid, easyFile.pid) && Objects.equals(parent_sid, easyFile.parent_sid) && Objects.equals(dataset_sid, easyFile.dataset_sid) && Objects.equals(path, easyFile.path) && Objects.equals(filename, easyFile.filename) && Objects.equals(mimetype, easyFile.mimetype) && Objects.equals(creator_role, easyFile.creator_role) && Objects.equals(visible_to, easyFile.visible_to) && Objects.equals(accessible_to, easyFile.accessible_to) && Objects.equals(sha1checksum, easyFile.sha1checksum);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pid, parent_sid, dataset_sid, path, filename, size, mimetype, creator_role, visible_to, accessible_to, sha1checksum);
  }
}
