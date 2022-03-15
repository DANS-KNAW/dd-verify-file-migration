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

import com.fasterxml.jackson.annotation.JsonProperty;

public class BagInfo {
  @JsonProperty("bag-id")
  private String bagId;

  @JsonProperty("base-id")
  private String baseId;

  private String created;
  private String doi;
  private String urn;

  public String getBagId() {
    return bagId;
  }

  public void setBagId(String bagId) {
    this.bagId = bagId;
  }

  public String getBaseId() {
    return baseId;
  }

  public void setBaseId(String baseId) {
    this.baseId = baseId;
  }

  public String getCreated() {
    return created;
  }

  public void setCreated(String created) {
    this.created = created;
  }

  public String getDoi() {
    return doi;
  }

  public void setDoi(String doi) {
    this.doi = doi;
  }

  public String getUrn() {
    return urn;
  }

  public void setUrn(String urn) {
    this.urn = urn;
  }

  @Override
  public String toString() {
    return "BagInfo{" +
            "bagId='" + bagId + '\'' +
            ", baseId='" + baseId + '\'' +
            ", created='" + created + '\'' +
            ", doi='" + doi + '\'' +
            ", urn='" + urn + '\'' +
            '}';
  }
}
