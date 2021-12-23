package nl.knaw.dans.filemigration.core;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BagInfoResult {

  @JsonProperty("bag-info")
  private BagInfo bagInfo;

  public BagInfo getBagInfo() {
    return bagInfo;
  }

  public void setBagInfo(BagInfo bagInfo) {
    this.bagInfo = bagInfo;
  }
}
