package nl.knaw.dans.filemigration.core;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class FilesXmlXpaths {
  private static final XPathFactory xpathfactory = XPathFactory.newInstance();
  public final XPathExpression accessibleTo = xpathfactory.newXPath().compile("//accessibleToRights/text()");
  public final XPathExpression visibleTo = xpathfactory.newXPath().compile("//visibleToRights/text()");
  public FilesXmlXpaths() throws XPathExpressionException {}
}
