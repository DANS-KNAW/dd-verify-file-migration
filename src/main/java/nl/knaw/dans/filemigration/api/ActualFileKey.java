package nl.knaw.dans.filemigration.api;

import java.io.Serializable;
import java.util.Objects;

public class ActualFileKey implements Serializable {
  private String doi;
  private String actual_path;
  private int version_nr;

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    ActualFileKey that = (ActualFileKey) o;
    return version_nr == that.version_nr && Objects.equals(doi, that.doi) && Objects.equals(actual_path, that.actual_path);
  }

  @Override
  public int hashCode() {
    return Objects.hash(doi, actual_path, version_nr);
  }
}
