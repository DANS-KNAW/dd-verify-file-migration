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

import java.io.Serializable;
import java.util.Objects;

public class ActualFileKey implements Serializable {
  private String doi;
  private String actual_path;
  private int major_version_nr;
  private int minor_version_nr;

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    ActualFileKey that = (ActualFileKey) o;
    return major_version_nr == that.major_version_nr && minor_version_nr == that.minor_version_nr && Objects.equals(doi, that.doi) && Objects.equals(actual_path, that.actual_path);
  }

  @Override
  public int hashCode() {
    return Objects.hash(doi, actual_path, major_version_nr, minor_version_nr);
  }
}
