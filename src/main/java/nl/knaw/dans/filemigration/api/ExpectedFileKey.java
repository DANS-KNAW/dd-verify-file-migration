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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

public class ExpectedFileKey implements Serializable {

  private String doi;
  private String expected_path;
  private int removed_duplicate_file_count;

  // Equals contract implementation generated with IntelliJ
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ExpectedFileKey)) return false;
    ExpectedFileKey that = (ExpectedFileKey) o;
    return new EqualsBuilder()
        .append(doi, that.doi)
        .append(expected_path, that.expected_path)
        .append(removed_duplicate_file_count, that.removed_duplicate_file_count)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .append(doi)
        .append(expected_path)
        .append(removed_duplicate_file_count).toHashCode();
  }
}
