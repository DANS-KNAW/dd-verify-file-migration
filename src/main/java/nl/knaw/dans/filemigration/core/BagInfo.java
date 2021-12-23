package nl.knaw.dans.filemigration.core;

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
    return "BagInfo{" + "bag_id='" + bagId + '\'' + ", base_id='" + baseId + '\'' + ", created='" + created + '\'' + ", doi='" + doi + '\'' + ", urn='" + urn + '\'' + '}';
  }
}
