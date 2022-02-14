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
package nl.knaw.dans.filemigration.core;

import org.hsqldb.lib.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Objects;

public class FileRights implements Serializable {
  private static final Logger log = LoggerFactory.getLogger(FileRights.class);

  private String accessibleTo;
  private String visibleTo;
  private String embargoDate;

  public String getEmbargoDate() {
    return embargoDate;
  }

  public void setEmbargoDate(String embargoDate) {
    this.embargoDate = embargoDate;
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

  public FileRights() {}

  public void setFileRights(String datasetAccessRights) {
    switch (datasetAccessRights) {
      case "OPEN_ACCESS":
        setAccessibleTo("ANONYMOUS");
        setVisibleTo("ANONYMOUS");
        break;
      case "OPEN_ACCESS_FOR_REGISTERED_USERS":
        setAccessibleTo("KNOWN");
        setVisibleTo("KNOWN");
        break;
      case "REQUEST_PERMISSION":
        setAccessibleTo("RESTRICTED_REQUEST");
        setVisibleTo("RESTRICTED_REQUEST");
        break;
      default:
        if (!"NO_ACCESS".equals(datasetAccessRights))
          log.warn("dataset rights not known: {}", datasetAccessRights);
        setAccessibleTo("NONE");
        setVisibleTo("NONE");
        break;
    }
  }

  public FileRights applyDefaults(FileRights defaultRights){
    if(StringUtil.isEmpty(getAccessibleTo()))
      setAccessibleTo(defaultRights.getAccessibleTo());
    if(StringUtil.isEmpty(getVisibleTo()))
      setVisibleTo(defaultRights.getVisibleTo());
    setEmbargoDate(defaultRights.getEmbargoDate());
    return this;
  }

  @Override
  public String toString() {
    return "FileRights{" +
            "accessibleTo='" + accessibleTo + '\'' +
            ", visibleTo='" + visibleTo + '\'' +
            ", embargoDate='" + embargoDate + '\'' +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FileRights that = (FileRights) o;
    return Objects.equals(accessibleTo, that.accessibleTo) && Objects.equals(visibleTo, that.visibleTo) && Objects.equals(embargoDate, that.embargoDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accessibleTo, visibleTo, embargoDate);
  }
}
