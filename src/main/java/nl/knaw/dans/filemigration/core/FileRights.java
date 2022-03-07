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
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class FileRights implements Serializable {
  private static final Logger log = LoggerFactory.getLogger(FileRights.class);
  private final String known = "KNOWN";
  private final String restricted_request = "RESTRICTED_REQUEST";
  private final String none = "NONE";
  private final String no_access = "NO_ACCESS";
  private final String open_access = "OPEN_ACCESS";
  private final String anonymous = "ANONYMOUS";
  private final String open_access_for_registered_users = "OPEN_ACCESS_FOR_REGISTERED_USERS";
  private final String request_permission = "REQUEST_PERMISSION";

  private String accessibleTo;
  private String visibleTo;
  private String embargoDate;

  public String getEmbargoDate() {
    return embargoDate;
  }

  public void setEmbargoDate(@Nullable String dateAvailable) {
    if (null != dateAvailable) {
      String s = dateAvailable.trim().replaceAll("\"", "");
      if (!StringUtil.isEmpty(s) && DateTime.now().compareTo(DateTime.parse(s)) < 0) {
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

  public void setFileRights(String datasetAccessRights) {
    switch (datasetAccessRights) {
      case open_access:
        setAccessibleTo(anonymous);
        setVisibleTo(anonymous);
        break;
      case open_access_for_registered_users:
        setAccessibleTo(known);
        setVisibleTo(known);
        break;
      case request_permission:
        setAccessibleTo(restricted_request);
        setVisibleTo(restricted_request);
        break;
      default:
        if (!"NO_ACCESS".equals(datasetAccessRights))
          log.warn("dataset rights not known: {}", datasetAccessRights);
        setAccessibleTo(none);
        setVisibleTo(none);
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
