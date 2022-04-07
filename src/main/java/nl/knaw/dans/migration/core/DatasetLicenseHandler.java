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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class DatasetLicenseHandler extends DefaultHandler {

  private static final Logger log = LoggerFactory.getLogger(DatasetLicenseHandler.class);

  static final String dansLicense = "https://dans.knaw.nl/en/about/organisation-and-policy/legal-information/DANSLicence.pdf";
  static final String cc0 = "http://creativecommons.org/publicdomain/zero/1.0";
  private static final SAXParserFactory parserFactory = configureFactory();
  private StringBuilder chars; // collected since the last startElement
  private String license = null;

  private static DatasetRights initDatasetRights() {
    DatasetRights datasetRights = new DatasetRights();
    datasetRights.setDefaultFileRights(new FileRights());
    return datasetRights;
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) {
    chars = new StringBuilder();
  }

  @Override
  public void endElement(String uri, String localName, String qName) {

    if ("license".equalsIgnoreCase(localName)) {
      String s = chars.toString();
      if(s.startsWith("http"))
        this.license = s;
    }
  }

  @Override
  public void characters(char[] ch, int start, int length) {
    chars.append(new String(ch, start, length));
  }

  private String get() {
    return license;
  }

  private static SAXParserFactory configureFactory() {
    SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
    saxParserFactory.setNamespaceAware(true);
    return saxParserFactory;
  }

  /**
   * @return key: filepath attribute of file elements
   * value: content of the elements: accessibleToRights and visibleToRights
   */
  static public String parseLicense(InputStream xml, AccessCategory accessCategory) {
    DatasetLicenseHandler handler = new DatasetLicenseHandler();
    try {
      parserFactory.newSAXParser().parse(xml, handler);
      return Optional.ofNullable(handler.get())
          .orElse(accessCategory.getDefaultLicense());
    }
    catch (ParserConfigurationException | SAXException | IOException e) {
      throw new RuntimeException(e);
    }
  }
}