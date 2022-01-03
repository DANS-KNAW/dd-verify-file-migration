package nl.knaw.dans.filemigration.core;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;

import static nl.knaw.dans.filemigration.core.FileRightsHandler.parseRights;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileRightsTest {

  @Test
  public void canParse() throws IOException {
    FileInputStream xml = new FileInputStream("src/test/resources/files.xml");
    FileRights fileRights = parseRights(xml).get("data/secret.txt");
    assertEquals("NONE", fileRights.getAccessibleTo());
    assertEquals("NONE", fileRights.getVisibleTo());
  }
}
