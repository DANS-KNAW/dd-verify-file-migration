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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  private static final Logger log = LoggerFactory.getLogger(FilesXml.class);

  private final Map<String, Node> files;
  private static final DocumentBuilderFactory docBuilderFactory = createFactory();
  private static final XPathFactory xpathFactory = XPathFactory.newInstance();
  private final Document doc;

  public FilesXml(String s) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
    log.trace(s);
    docBuilderFactory.setNamespaceAware(true); // never forget this!
    doc = docBuilderFactory.newDocumentBuilder().parse(new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)));
    NodeList nodes = (NodeList) xpathFactory.newXPath().compile("//file").evaluate(doc, XPathConstants.NODESET);
    files = new HashMap<>();
    for (int i=0; i<nodes.getLength(); i++) {
      Node node = nodes.item(i);
      String path = node.getAttributes().getNamedItem("filepath").getNodeValue();
      files.put(path, node);
    }
  }

  public String get(String path, String typeOfRights) {
    char q = '"';
    String s = "//file[@filepath=" + q + path + q + "]/" + typeOfRights + "ToRights/text()";
    try {
      XPathExpression expr = xpathFactory.newXPath().compile(s);
      NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
      if(nodes.getLength() > 0)
        return nodes.item(0).getNodeValue();
      else {
        log.error("could not find {}", s);
        return "";
      }
    }
    catch (XPathExpressionException e) {
      throw new RuntimeException(e);
    }
  }

  private static DocumentBuilderFactory createFactory() {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true); // never forget this!
    return factory;
  }
}
