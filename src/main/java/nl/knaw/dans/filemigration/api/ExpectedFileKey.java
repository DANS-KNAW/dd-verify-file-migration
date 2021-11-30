package nl.knaw.dans.filemigration.api;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

public class ExpectedFileKey implements Serializable {

  private String doi;
  private String expected_path;
  private int removed_duplicate_file_count;
  private boolean removed_original_directory;

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
        .append(removed_original_directory, that.removed_original_directory)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .append(doi)
        .append(expected_path)
        .append(removed_duplicate_file_count)
        .append(removed_original_directory).toHashCode();
  }
}
