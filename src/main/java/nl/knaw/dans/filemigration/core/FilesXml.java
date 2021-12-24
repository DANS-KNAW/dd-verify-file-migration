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
package nl.knaw.dans.filemigration.core;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class FilesXml {

  private static final DocumentBuilderFactory factory = createFactory();
  private final Map<String, Node> files;
  private final XPathFactory xpathfactory = XPathFactory.newInstance();
  final XPathExpression ACCESSIBLE_TO = xpathfactory.newXPath().compile("accessibleToRights/text()");
  final XPathExpression VISIBLE_TO = xpathfactory.newXPath().compile("visibleToRights/text()");

  public FilesXml(String s) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
    Document doc = factory.newDocumentBuilder().parse(new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)));
    NodeList nodes = (NodeList) xpathfactory.newXPath().compile("//file").evaluate(doc, XPathConstants.NODESET);
    files = Collections.emptyMap();
    for (int i=0; i<nodes.getLength(); i++) {
      Node node = nodes.item(i);
      String path = node.getAttributes().getNamedItem("filePath").getNodeValue();
      files.put(path, node);
    }
  }

  String get(String path, XPathExpression expr) throws XPathExpressionException {
    NodeList nodes = (NodeList) expr.evaluate(files.get(path), XPathConstants.NODESET);
    return nodes.item(0).getNodeValue();
  }

  private static DocumentBuilderFactory createFactory() {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true); // never forget this!
    return factory;
  }
}
