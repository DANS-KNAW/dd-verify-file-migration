package nl.knaw.dans.filemigration.core;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FilesXmlTest {

  @Test
  public void x() throws IOException, XPathExpressionException, ParserConfigurationException, SAXException {
    FilesXml filesXml = new FilesXml(FileUtils.readFileToString(new File("src/test/resources/files.xml"), Charset.defaultCharset()));
    assertEquals("NONE",filesXml.get("data/secret.txt", "accessible"));
  }
}
