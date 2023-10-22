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
package nl.knaw.dans.migration.core;

import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Objects;

public class FileRights implements Serializable {

  private String accessibleTo;
  private String visibleTo;
  private String embargoDate;

  public String getEmbargoDate() {
    return embargoDate;
  }

  public void setEmbargoDate(@Nullable String dateAvailable) {
    if (null != dateAvailable) {
      String s = dateAvailable.trim().replaceAll("\"", "");
      if (!StringUtils.isEmpty(s) && DateTime.now().compareTo(DateTime.parse(s)) < 0) {
        this.embargoDate = dateAvailable.trim();
      }
    }
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

  public void setFileRights(AccessCategory rights) {
    String fileRights = rights.getFileRights();
    setAccessibleTo(fileRights);
    setVisibleTo(AccessCategory.OPEN_ACCESS.getFileRights());
  }

  public FileRights applyDefaults(FileRights defaultRights){
    if(StringUtils.isEmpty(getAccessibleTo()))
      setAccessibleTo(defaultRights.getAccessibleTo());
    if(StringUtils.isEmpty(getVisibleTo()))
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
