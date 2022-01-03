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
