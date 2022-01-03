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
import java.util.HashMap;
import java.util.Map;

public class FilesXml {

  private final Map<String, Node> files;
  private static final DocumentBuilderFactory docBuilderFactory = createFactory();
  private static final XPathFactory xpathFactory = XPathFactory.newInstance();

  public FilesXml(String s) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
    docBuilderFactory.setNamespaceAware(true); // never forget this!
    Document doc = docBuilderFactory.newDocumentBuilder().parse(new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)));
    NodeList nodes = (NodeList) xpathFactory.newXPath().compile("//file").evaluate(doc, XPathConstants.NODESET);
    files = new HashMap<>();
    for (int i=0; i<nodes.getLength(); i++) {
      Node node = nodes.item(i);
      String path = node.getAttributes().getNamedItem("filepath").getNodeValue();
      files.put(path, node);
    }
  }

  public String get(String path, XPathExpression expr) {
    try {
      Node node = files.get(path);
      NodeList nodes = (NodeList) expr.evaluate(node, XPathConstants.NODESET);
      return nodes.item(0).getNodeValue();
    }
    catch (XPathExpressionException e) {
      throw new RuntimeException(e);
    }
    catch (NullPointerException e) {
      throw new RuntimeException(path + " not found");
    }
  }

  private static DocumentBuilderFactory createFactory() {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true); // never forget this!
    return factory;
  }
}
