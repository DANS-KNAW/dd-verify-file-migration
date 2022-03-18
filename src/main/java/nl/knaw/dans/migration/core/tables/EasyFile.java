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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.Table;
import java.util.Objects;

// order by in decreasing numeric order, thus earlier versions become duplicates
@NamedQueries({ @NamedQuery(name = "EasyFile.findByDatasetId",
                            query = "SELECT ef FROM EasyFile ef WHERE ef.datasetSid = :datasetSid ORDER BY length(ef.pid) desc, ef.pid desc",
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
    public static final String FIND_BY_DATASET_ID = "EasyFile.findByDatasetId";
    public static final String DATASET_ID = "datasetSid";

    public EasyFile() {
    }

    @Id
    @Column(nullable = false)
    private String pid = "";

    @Column(name="parent_sid",nullable = false)
    private String parentSid = "";

    @Column(name="dataset_sid",nullable = false)
    private String datasetSid = "";

    @Column()
    private String path = "";

    @Column(nullable = false)
    private String filename = "";

    @Column(nullable = false)
    private long size;

    @Column(nullable = false)
    private String mimetype = "";

    @Column(name="creator_role",nullable = false)
    private String creatorRole = "";

    @Column(name="visible_to",nullable = false)
    private String visibleTo = "";

    @Column(name="accessible_to",nullable = false)
    private String accessibleTo = "";

    @Column(name="sha1checksum")
    private String sha1Checksum = "";

    @Override
    public String toString() {
        return "EasyFile{" +
                "pid='" + pid + '\'' +
                ", parentSid='" + parentSid + '\'' +
                ", datasetSid='" + datasetSid + '\'' +
                ", path='" + path + '\'' +
                ", filename='" + filename + '\'' +
                ", size=" + size +
                ", mimetype='" + mimetype + '\'' +
                ", creatorRole='" + creatorRole + '\'' +
                ", visibleTo='" + visibleTo + '\'' +
                ", accessibleTo='" + accessibleTo + '\'' +
                ", sha1Checksum='" + sha1Checksum + '\'' +
                '}';
    }

    public String getSha1Checksum() {
        return sha1Checksum;
    }

    public void setSha1Checksum(String sha1Checksum) {
        this.sha1Checksum = sha1Checksum;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EasyFile easyFile = (EasyFile) o;
        return size == easyFile.size && Objects.equals(pid, easyFile.pid) && Objects.equals(parentSid, easyFile.parentSid) && Objects.equals(datasetSid, easyFile.datasetSid) && Objects.equals(path, easyFile.path) && Objects.equals(filename, easyFile.filename) && Objects.equals(mimetype, easyFile.mimetype) && Objects.equals(creatorRole, easyFile.creatorRole) && Objects.equals(visibleTo, easyFile.visibleTo) && Objects.equals(accessibleTo, easyFile.accessibleTo) && Objects.equals(sha1Checksum, easyFile.sha1Checksum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pid, parentSid, datasetSid, path, filename, size, mimetype, creatorRole, visibleTo, accessibleTo, sha1Checksum);
    }
}
