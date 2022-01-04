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

import java.io.Serializable;
import java.util.Objects;

public class FileRights implements Serializable {
  private String accessibleTo;
  private String visibleTo;

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

  @Override
  public String toString() {
    return "FileMetadata{" + "accessibleTo='" + accessibleTo + '\'' + ", visibleTo='" + visibleTo + '\'' + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    FileRights that = (FileRights) o;
    return Objects.equals(accessibleTo, that.accessibleTo) && Objects.equals(visibleTo, that.visibleTo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accessibleTo, visibleTo);
  }
}
