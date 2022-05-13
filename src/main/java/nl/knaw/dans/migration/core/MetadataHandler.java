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

public class MetadataHandler extends DefaultHandler {

  private static final Logger log = LoggerFactory.getLogger(MetadataHandler.class);

  static final String dansLicense = "https://dans.knaw.nl/en/about/organisation-and-policy/legal-information/DANSLicence.pdf";
  static final String cc0 = "http://creativecommons.org/publicdomain/zero/1.0";
  private static final SAXParserFactory parserFactory = configureFactory();
  private StringBuilder chars; // collected since the last startElement
  private String license = null;
  private String created = null;
  private AccessCategory accessCategory;

  /** applies to EMD as well as DDM TODO even DatasetRightsHandler might apply to both  */
  class DatasetMetadata {
    final String license;
    final String created;

    DatasetMetadata(String license, String created) {
      this.license = license;
      this.created = created;
    }
  }
  private MetadataHandler(AccessCategory defaultAccessCategory){
    this.accessCategory = defaultAccessCategory;
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
    } else if ("created".equalsIgnoreCase(localName))
        this.created = chars.toString();
  }

  @Override
  public void characters(char[] ch, int start, int length) {
    chars.append(new String(ch, start, length));
  }

  private DatasetMetadata get() {
    return new DatasetMetadata(Optional.ofNullable(license)
        .orElse(accessCategory.getDefaultLicense()), created);
  }

  private static SAXParserFactory configureFactory() {
    SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
    saxParserFactory.setNamespaceAware(true);
    return saxParserFactory;
  }

  static public DatasetMetadata parse(InputStream xml, AccessCategory accessCategory) {
    MetadataHandler handler = new MetadataHandler(accessCategory);
    try {
      parserFactory.newSAXParser().parse(xml, handler);
      return handler.get();
    }
    catch (ParserConfigurationException | SAXException | IOException e) {
      throw new RuntimeException(e);
    }
  }
}