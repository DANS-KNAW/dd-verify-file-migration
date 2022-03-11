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


package nl.knaw.dans.migration.core;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class FileRightsHandler extends DefaultHandler {

  private static final SAXParserFactory parserFactory = configureFactory();
  private final Map<String, FileRights> map = new HashMap<>();
  private StringBuilder chars; // collected since the last startElement
  private FileRights fileRights; // collected since startElement of the last <file>

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) {

    if ("file".equalsIgnoreCase(qName)) {
      String path = attributes.getValue("filepath");
      fileRights = new FileRights();
      map.put(path, fileRights);
    }
    chars = new StringBuilder();
  }

  @Override
  public void endElement(String uri, String localName, String qName) {

    if ("accessibleToRights".equalsIgnoreCase(qName)) {
      fileRights.setAccessibleTo(chars.toString());
    }
    else if ("visibleToRights".equalsIgnoreCase(qName)) {
      fileRights.setVisibleTo(chars.toString());
    }
  }

  @Override
  public void characters(char[] ch, int start, int length) {
    chars.append(new String(ch, start, length));
  }

  private Map<String, FileRights> get() {
    return map;
  }

  static private SAXParserFactory configureFactory() {
    SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
    saxParserFactory.setNamespaceAware(true);
    return saxParserFactory;
  }

  /**
   * @return key: filepath attribute of file elements
   *         value: content of the elements: accessibleToRights and visibleToRights
   */
  static public Map<String, FileRights> parseRights(InputStream xml) {
    FileRightsHandler handler = new FileRightsHandler();
    try {
      parserFactory.newSAXParser().parse(xml, handler);
      return handler.get();
    }
    catch (ParserConfigurationException | SAXException | IOException e) {
      throw new RuntimeException(e);
    }
  }
}