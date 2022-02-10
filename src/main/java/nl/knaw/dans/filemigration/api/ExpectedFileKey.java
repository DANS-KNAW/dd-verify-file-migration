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
import java.util.Objects;

public class ExpectedFileKey implements Serializable {

  private String doi;
  private String expectedPath;
  private int removedDuplicateFileCount;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ExpectedFileKey that = (ExpectedFileKey) o;
    return removedDuplicateFileCount == that.removedDuplicateFileCount && Objects.equals(doi, that.doi) && Objects.equals(expectedPath, that.expectedPath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(doi, expectedPath, removedDuplicateFileCount);
  }
}
